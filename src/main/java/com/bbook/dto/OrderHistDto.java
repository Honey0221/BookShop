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

	public OrderHistDto(Order order) {
		this.orderId = order.getId();
		this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		this.orderStatus = order.getOrderStatus();
		
		order.getOrderBooks().forEach(orderBook -> {
			OrderBookDto orderBookDto = new OrderBookDto(orderBook, orderBook.getBook().getImageUrl());
			orderBookDtoList.add(orderBookDto);
		});
	}

	public void addOrderBookDto(OrderBookDto orderBookDto) {
		orderBookDtoList.add(orderBookDto);
	}
}
