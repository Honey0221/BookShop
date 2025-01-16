package com.bbook.dto;

import com.bbook.constant.OrderStatus;
import com.bbook.entity.Order;
import com.bbook.entity.OrderBook;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class AdminOrderDto {
    private String merchantUid;
    private String memberName;
    private String email;
    private String orderName;
    private Long totalPrice;
    private Long shippingFee;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private boolean subscriber;
    private List<OrderBookDto> orderBooks;

    @Getter
    @Setter
    public static class OrderBookDto {
        private String bookTitle;
        private int count;
        private int price;
        private int totalPrice;

        public static OrderBookDto of(OrderBook orderBook) {
            OrderBookDto dto = new OrderBookDto();
            dto.bookTitle = orderBook.getBook().getTitle();
            dto.count = orderBook.getCount();
            dto.price = orderBook.getOrderPrice();
            dto.totalPrice = orderBook.getTotalPrice();
            return dto;
        }
    }

    public static AdminOrderDto of(Order order) {
        AdminOrderDto dto = new AdminOrderDto();
        dto.merchantUid = order.getMerchantUid();
        dto.memberName = order.getMember().getNickname();
        dto.email = order.getMember().getEmail();
        dto.orderName = !order.getOrderBooks().isEmpty() ? order.getOrderBooks().get(0).getBook().getTitle() +
                (order.getOrderBooks().size() > 1 ? " 외 " + (order.getOrderBooks().size() - 1) + "건" : "") : "주문 상품 없음";
        dto.totalPrice = order.getTotalPrice();
        dto.shippingFee = order.getShippingFee();
        dto.orderStatus = order.getOrderStatus();
        dto.orderDate = order.getOrderDate();
        dto.orderBooks = order.getOrderBooks().stream()
                .map(OrderBookDto::of)
                .collect(Collectors.toList());
        return dto;
    }
}