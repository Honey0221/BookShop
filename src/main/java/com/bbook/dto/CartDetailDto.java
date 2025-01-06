package com.bbook.dto;

import com.bbook.entity.CartItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailDto {
	private Long cartItemId;
	private Long bookId;
	private String bookName;
	private int price;
	private int count;
	private String imageUrl;

	public CartDetailDto(Long cartItemId, String bookName, int price, int count) {
		this.cartItemId = cartItemId;
		this.bookName = bookName;
		this.price = price;
		this.count = count;
	}

	public CartDetailDto(CartItem cartItem) {
		this.cartItemId = cartItem.getId();
		this.bookId = cartItem.getBook().getId();
		this.bookName = cartItem.getBook().getTitle();
		this.price = cartItem.getBook().getPrice();
		this.count = cartItem.getCount();
		this.imageUrl = cartItem.getBook().getImageUrl();
	}

	public CartDetailDto(Long cartItemId, String bookName, int price, int count, String imageUrl) {
		this.cartItemId = cartItemId;
		this.bookName = bookName;
		this.price = price;
		this.count = count;
		this.imageUrl = imageUrl;
	}
}
