package com.bbook.service;

import com.bbook.constant.OrderStatus;
import com.bbook.dto.AdminOrderDto;
import com.bbook.dto.OrderSearchDto;
import com.bbook.entity.Order;
import com.bbook.repository.OrderRepository;
import com.bbook.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Page<AdminOrderDto> searchOrders(OrderSearchDto searchDto, Pageable pageable) {
        log.info("주문 검색 시작 - 검색 조건: {}", searchDto);
        return orderRepository.searchOrders(searchDto, pageable)
                .map(AdminOrderDto::of);
    }

    public AdminOrderDto getOrderDetail(String merchantUid) {
        try {
            log.info("주문 상세 조회 시작 - merchantUid: {}", merchantUid);
            Order order = orderRepository.findByMerchantUid(merchantUid)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
            return AdminOrderDto.of(order);
        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류가 발생했습니다.", e);
            throw new RuntimeException("주문 상세 조회 중 오류가 발생했습니다.", e);
        }
    }

    public Map<String, Object> getOrderSummary(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("주문 요약 통계 조회 시작 - 기간: {} ~ {}", startDate, endDate);

            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

            Map<String, Object> summary = new HashMap<>();

            // 전체 주문 수
            long totalOrders = orders.size();
            summary.put("totalOrders", totalOrders);

            // 평균 일일 주문 수
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            double avgDailyOrders = totalOrders / (double) days;
            summary.put("avgDailyOrders", Math.round(avgDailyOrders * 100.0) / 100.0);

            // 구독자 주문 비율
            long subscriberOrders = orders.stream()
                    .filter(order -> subscriptionRepository.existsByMemberAndEndDateAfter(
                            order.getMember(), order.getOrderDate()))
                    .count();
            double subscriberOrderRate = totalOrders > 0 ? (subscriberOrders * 100.0) / totalOrders : 0;
            summary.put("subscriberOrderRate", Math.round(subscriberOrderRate * 100.0) / 100.0);

            // 주문 취소율
            long canceledOrders = orders.stream()
                    .filter(order -> order.getOrderStatus() == OrderStatus.CANCEL)
                    .count();
            double cancelRate = totalOrders > 0 ? (canceledOrders * 100.0) / totalOrders : 0;
            summary.put("cancelRate", Math.round(cancelRate * 100.0) / 100.0);

            return summary;
        } catch (Exception e) {
            log.error("주문 요약 통계 조회 중 오류가 발생했습니다.", e);
            throw new RuntimeException("주문 요약 통계 조회 중 오류가 발생했습니다.", e);
        }
    }

    public Map<String, Object> getProductStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("상품 통계 조회 시작 - 기간: {} ~ {}", startDate, endDate);

            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

            Map<String, Object> stats = new HashMap<>();

            // 상위 10개 상품
            Map<String, Long> productCounts = orders.stream()
                    .filter(order -> order.getOrderStatus() != OrderStatus.CANCEL)
                    .flatMap(order -> order.getOrderBooks().stream())
                    .collect(Collectors.groupingBy(
                            orderBook -> orderBook.getBook().getTitle(),
                            Collectors.summingLong(orderBook -> orderBook.getCount())));

            List<Map<String, Object>> topProducts = productCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("name", entry.getKey());
                        product.put("count", entry.getValue());
                        return product;
                    })
                    .collect(Collectors.toList());

            stats.put("topProducts", topProducts);

            // 카테고리별 판매 비율
            Map<String, Long> categoryStats = orders.stream()
                    .filter(order -> order.getOrderStatus() != OrderStatus.CANCEL)
                    .flatMap(order -> order.getOrderBooks().stream())
                    .collect(Collectors.groupingBy(
                            orderBook -> orderBook.getBook().getMainCategory(),
                            Collectors.summingLong(orderBook -> orderBook.getCount())));

            stats.put("categoryStats", categoryStats);

            return stats;
        } catch (Exception e) {
            log.error("상품 통계 조회 중 오류가 발생했습니다.", e);
            throw new RuntimeException("상품 통계 조회 중 오류가 발생했습니다.", e);
        }
    }

    public Map<String, Object> getSubscriptionStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("구독 통계 조회 시작 - 기간: {} ~ {}", startDate, endDate);

            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

            Map<String, Object> stats = new HashMap<>();

            // 구독자/비구독자 주문 수
            Map<Boolean, Long> subscriptionStats = orders.stream()
                    .filter(order -> order.getOrderStatus() != OrderStatus.CANCEL)
                    .collect(Collectors.groupingBy(
                            order -> subscriptionRepository.existsByMemberAndEndDateAfter(
                                    order.getMember(), order.getOrderDate()),
                            Collectors.counting()));

            stats.put("subscriberCount", subscriptionStats.getOrDefault(true, 0L));
            stats.put("nonSubscriberCount", subscriptionStats.getOrDefault(false, 0L));

            // 시간대별 주문 패턴
            Map<Integer, Map<Boolean, Long>> hourlyPattern = orders.stream()
                    .filter(order -> order.getOrderStatus() != OrderStatus.CANCEL)
                    .collect(Collectors.groupingBy(
                            order -> order.getOrderDate().getHour(),
                            Collectors.groupingBy(
                                    order -> subscriptionRepository.existsByMemberAndEndDateAfter(
                                            order.getMember(), order.getOrderDate()),
                                    Collectors.counting())));

            stats.put("hourlyPattern", hourlyPattern);

            return stats;
        } catch (Exception e) {
            log.error("구독 통계 조회 중 오류가 발생했습니다.", e);
            throw new RuntimeException("구독 통계 조회 중 오류가 발생했습니다.", e);
        }
    }
}
