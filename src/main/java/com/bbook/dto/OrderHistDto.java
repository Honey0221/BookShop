package com.bbook.dto;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.bbook.constant.OrderStatus;
import com.bbook.entity.Order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderHistDto {
	private Long orderId;
	private String orderDate;
	private OrderStatus orderStatus;
	private List<OrderBookDto> orderBookDtoList = new ArrayList<>();
	private boolean isSubscriber;
	private Long shippingFee;
	private Integer usedPoints;
	private Integer discountAmount;
	private Long totalPrice;

	public OrderHistDto(Order order) {
		this.orderId = order.getId();
		this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		this.orderStatus = order.getOrderStatus();
		this.isSubscriber = order.getMember().isSubscriber();
		this.shippingFee = order.getShippingFee();
		this.usedPoints = order.getUsedPoints();
		this.discountAmount = order.getDiscountAmount();
		this.totalPrice = order.getTotalPrice();

		order.getOrderBooks().forEach(orderBook -> {
			OrderBookDto orderBookDto = new OrderBookDto(orderBook, orderBook.getBook().getImageUrl());
			orderBookDtoList.add(orderBookDto);
		});
	}

	public void addOrderBookDto(OrderBookDto orderBookDto) {
		orderBookDtoList.add(orderBookDto);
	}
}
