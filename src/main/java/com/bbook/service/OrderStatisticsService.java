package com.bbook.service;

import com.bbook.constant.OrderStatus;
import com.bbook.dto.OrderStatisticsDto;
import com.bbook.dto.OrderStatisticsDto.*;
import com.bbook.entity.Order;
import com.bbook.entity.Subscription;
import com.bbook.repository.OrderRepository;
import com.bbook.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatisticsService {

    private final OrderRepository orderRepository;
    private final SubscriptionRepository subscriptionRepository;

    public OrderStatisticsDto getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating order statistics from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

        // 해당 기간의 모든 주문 조회
        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);
        log.info("Found {} orders in the period", orders.size());

        OrderStatisticsDto statistics = OrderStatisticsDto.builder()
                .summary(calculateOrderSummary(orders, startDate, endDate))
                .topProducts(calculateTopProducts(orders))
                .categoryStats(calculateCategoryStats(orders))
                .subscriptionStats(calculateSubscriptionStats(orders))
                .orderPattern(calculateOrderPattern(orders))
                .build();

        log.info("Statistics calculated: {} top products, {} categories, {} subscriber vs {} non-subscriber orders",
                statistics.getTopProducts().size(),
                statistics.getCategoryStats().size(),
                statistics.getSubscriptionStats().getSubscriber(),
                statistics.getSubscriptionStats().getNonSubscriber());

        return statistics;
    }

    private OrderSummaryDto calculateOrderSummary(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        long totalOrders = orders.size();
        long nonCanceledOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.CANCEL)
                .count();

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double avgDailyOrders = totalOrders / (double) days;

        long subscriberOrders = orders.stream()
                .filter(order -> {
                    Subscription subscription = subscriptionRepository.findByMemberId(order.getMember().getId())
                            .orElse(null);
                    return subscription != null && subscription.isActive() &&
                            subscription.getEndDate().isAfter(order.getOrderDate());
                })
                .count();

        double subscriberOrderRate = totalOrders > 0 ? (subscriberOrders * 100.0) / totalOrders : 0;

        long canceledOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.CANCEL)
                .count();

        double cancelRate = totalOrders > 0 ? (canceledOrders * 100.0) / totalOrders : 0;

        log.info("Order summary - Total: {}, Non-canceled: {}, Avg Daily: {}, Subscriber Rate: {}%, Cancel Rate: {}%",
                totalOrders, nonCanceledOrders, avgDailyOrders, subscriberOrderRate, cancelRate);

        return OrderSummaryDto.builder()
                .totalOrders(totalOrders)
                .avgDailyOrders(avgDailyOrders)
                .subscriberOrderRate(subscriberOrderRate)
                .cancelRate(cancelRate)
                .build();
    }

    private List<ProductStatsDto> calculateTopProducts(List<Order> orders) {
        Map<String, Integer> productQuantities = new HashMap<>();

        orders.stream()
                .flatMap(order -> {
                    log.info("Order {} (status: {}) has {} orderBooks",
                            order.getId(),
                            order.getOrderStatus(),
                            order.getOrderBooks().size());
                    return order.getOrderBooks().stream();
                })
                .forEach(orderBook -> {
                    String bookTitle = orderBook.getBook().getTitle();
                    log.info("OrderBook for book: {}, count: {}, order status: {}",
                            bookTitle,
                            orderBook.getCount(),
                            orderBook.getOrder().getOrderStatus());
                    productQuantities.merge(bookTitle, orderBook.getCount(), Integer::sum);
                });

        log.info("Found {} unique products in orders", productQuantities.size());

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    log.info("Top product: {} with quantity: {}", entry.getKey(), entry.getValue());
                    return ProductStatsDto.builder()
                            .name(entry.getKey())
                            .quantity(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CategoryStatsDto> calculateCategoryStats(List<Order> orders) {
        log.info("Calculating category stats for {} orders", orders.size());
        Map<String, Integer> categoryCount = new HashMap<>();

        orders.stream()
                .flatMap(order -> {
                    log.info("Processing order {} with status {} and {} books",
                            order.getId(),
                            order.getOrderStatus(),
                            order.getOrderBooks().size());
                    return order.getOrderBooks().stream();
                })
                .forEach(orderBook -> {
                    String category = orderBook.getBook().getMainCategory();
                    int count = orderBook.getCount();
                    log.info("Adding {} items from category {} (Book: {})",
                            count,
                            category,
                            orderBook.getBook().getTitle());
                    categoryCount.merge(category, count, Integer::sum);
                });

        log.info("Found {} unique categories in orders", categoryCount.size());
        categoryCount.forEach((category, count) -> log.info("Category total - {}: {} items", category, count));

        return categoryCount.entrySet().stream()
                .map(entry -> {
                    log.info("Creating CategoryStatsDto for {} with count {}", entry.getKey(), entry.getValue());
                    return CategoryStatsDto.builder()
                            .category(entry.getKey())
                            .count(entry.getValue())
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    private SubscriptionStatsDto calculateSubscriptionStats(List<Order> orders) {
        log.info("Calculating subscription stats for {} orders", orders.size());

        Map<Boolean, Long> subscriptionCount = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> {
                            Subscription subscription = subscriptionRepository.findByMemberId(order.getMember().getId())
                                    .orElse(null);
                            boolean isSubscriber = subscription != null && subscription.isActive() &&
                                    subscription.getEndDate().isAfter(order.getOrderDate());
                            log.info("Order {} - Member {} is subscriber: {}, subscription end date: {}",
                                    order.getId(),
                                    order.getMember().getId(),
                                    isSubscriber,
                                    subscription != null ? subscription.getEndDate() : "No subscription");
                            return isSubscriber;
                        },
                        Collectors.counting()));

        int subscriberCount = subscriptionCount.getOrDefault(true, 0L).intValue();
        int nonSubscriberCount = subscriptionCount.getOrDefault(false, 0L).intValue();

        log.info("Subscription stats - Subscribers: {}, Non-subscribers: {}", subscriberCount, nonSubscriberCount);

        return SubscriptionStatsDto.builder()
                .subscriber(subscriberCount)
                .nonSubscriber(nonSubscriberCount)
                .build();
    }

    private List<OrderPatternDto> calculateOrderPattern(List<Order> orders) {
        Map<Integer, int[]> hourlyStats = new HashMap<>();

        // Initialize hourly stats for all 24 hours
        for (int i = 0; i < 24; i++) {
            hourlyStats.put(i, new int[] { 0, 0 }); // [subscriberCount, nonSubscriberCount]
        }

        orders.stream()
                .forEach(order -> {
                    int hour = order.getOrderDate().getHour();
                    boolean isSubscriber = subscriptionRepository.findByMemberId(order.getMember().getId())
                            .map(subscription -> subscription.isActive() &&
                                    subscription.getEndDate().isAfter(order.getOrderDate()))
                            .orElse(false);

                    int[] counts = hourlyStats.get(hour);
                    if (isSubscriber) {
                        counts[0]++;
                    } else {
                        counts[1]++;
                    }
                });

        return hourlyStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> OrderPatternDto.builder()
                        .hour(entry.getKey())
                        .subscriberCount(entry.getValue()[0])
                        .nonSubscriberCount(entry.getValue()[1])
                        .build())
                .collect(Collectors.toList());
    }
}