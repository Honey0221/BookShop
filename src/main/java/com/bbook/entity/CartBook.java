package com.bbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cart_book")
public class CartBook {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "cart_book_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id")
	private Book book;
	private int count;

	public static CartBook createCartBook(Cart cart, Book book, int count) {
		CartBook cartBook = new CartBook();
		cartBook.setCart(cart);
		cartBook.setBook(book);
		cartBook.setCount(count);
		return cartBook;
	}

	public void addCount(int count) {
		this.count += count;
	}

	public void updateCount(int count) {
		this.count = count;
	}
}
