package com.bbook.dto;

import com.bbook.entity.OrderBook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderBookDto {
	private String bookName;
	private int count;
	private int orderPrice;
	private String imgUrl;

	public OrderBookDto(OrderBook orderBook, String imgUrl) {
		this.bookName = orderBook.getBook().getTitle();
		this.count = orderBook.getCount();
		this.orderPrice = orderBook.getOrderPrice();
		this.imgUrl = imgUrl;
	}
}
