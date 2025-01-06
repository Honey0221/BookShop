package com.bbook.dto;

import com.bbook.entity.OrderItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
	private String bookName;
	private int count;
	private int orderPrice;
	private String imgUrl;

	public OrderItemDto(OrderItem orderItem, String imgUrl) {
		this.bookName = orderItem.getBook().getTitle();
		this.count = orderItem.getCount();
		this.orderPrice = orderItem.getOrderPrice();
		this.imgUrl = imgUrl;
	}
}
