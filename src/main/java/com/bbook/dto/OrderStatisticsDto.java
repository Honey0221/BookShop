package com.bbook.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderStatisticsDto {
    private OrderSummaryDto summary;
    private List<ProductStatsDto> topProducts;
    private List<CategoryStatsDto> categoryStats;
    private SubscriptionStatsDto subscriptionStats;
    private List<OrderPatternDto> orderPattern;

    @Getter
    @Setter
    @Builder
    public static class OrderSummaryDto {
        private long totalOrders;
        private double avgDailyOrders;
        private double subscriberOrderRate;
        private double cancelRate;
    }

    @Getter
    @Setter
    @Builder
    public static class ProductStatsDto {
        private String name;
        private int quantity;
    }

    @Getter
    @Setter
    @Builder
    public static class CategoryStatsDto {
        private String category;
        private int count;
    }

    @Getter
    @Setter
    @Builder
    public static class SubscriptionStatsDto {
        private int subscriber;
        private int nonSubscriber;
    }

    @Getter
    @Setter
    @Builder
    public static class OrderPatternDto {
        private int hour;
        private int subscriberCount;
        private int nonSubscriberCount;
    }
}