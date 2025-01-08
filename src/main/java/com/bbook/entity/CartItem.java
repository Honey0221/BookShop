package com.bbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "cart_item_id")
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id")
	private Book book;
	private int count;

	public static CartItem createCartItem(Cart cart, Book book, int count) {
		CartItem cartItem = new CartItem();
		cartItem.setCart(cart);
		cartItem.setBook(book);
		cartItem.setCount(count);
		return cartItem;
	}

	public void addCount(int count) {
		this.count += count;
	}

	public void updateCount(int count) {
		this.count = count;
	}
}
