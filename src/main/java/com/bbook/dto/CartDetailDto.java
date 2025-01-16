package com.bbook.dto;

import com.bbook.entity.CartBook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailDto {
	private Long cartBookId;
	private String bookName;
	private int price;
	private int count;
	private String imageUrl;
	private int stock;

	public CartDetailDto(Long cartBookId, String bookName, int price, int count, String imageUrl, int stock) {
		this.cartBookId = cartBookId;
		this.bookName = bookName;
		this.price = price;
		this.count = count;
		this.imageUrl = imageUrl;
		this.stock = stock;
	}

	public CartDetailDto(CartBook cartBook) {
		this.cartBookId = cartBook.getId();
		this.bookName = cartBook.getBook().getTitle();
		this.price = cartBook.getBook().getPrice();
		this.count = cartBook.getCount();
		this.imageUrl = cartBook.getBook().getImageUrl();
		this.stock = cartBook.getBook().getStock();
	}
}
