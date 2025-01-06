package com.bbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrderItem extends BaseEntity {
	@Id
	@GeneratedValue
	@Column(name = "order_book_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id") // 외래키
	private Book book;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id") // 외래키
	private Order order;
	private int orderPrice; // 주문가격
	private int count; // 수량
	// private LocalDateTime regTime;
	// private LocalDateTime updateTime;

	public static OrderItem createOrderItem(Book book, int count) {
		OrderItem orderItem = new OrderItem();
		orderItem.setBook(book);
		orderItem.setCount(count);
		orderItem.setOrderPrice(book.getPrice());
		book.removeStock(count);
		return orderItem;
	}

	public int getTotalPrice() {
		return orderPrice * count;
	}

	public void cancel() {
		this.getBook().addStock(count);
	}
}
