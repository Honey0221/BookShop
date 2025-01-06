package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.BookStatus;
import com.bbook.dto.BookFormDto;
import com.bbook.exception.OutOfStockException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "book_status")
  @Enumerated(EnumType.STRING)
  private BookStatus bookStatus;

  @Column(nullable = false)
  private Integer price;

  @Column
  private Integer stock;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private String author;

  @Column(name = "detail_category")
  private String detailCategory;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Column(name = "main_category", nullable = false)
  private String mainCategory;

  @Column(name = "mid_category", nullable = false)
  private String midCategory;

  @Column(nullable = false)
  private String publisher;

  @Column(name = "sub_category")
  private String subCategory;

  @Column(nullable = false)
  private String title;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String description;

  @Column(name = "view_count", columnDefinition = "bigint default 0")
  private Long viewCount;

  public void updateBook(BookFormDto bookFormDto) {
    this.title = bookFormDto.getTitle();
    this.author = bookFormDto.getAuthor();
    this.publisher = bookFormDto.getPublisher();
    this.price = bookFormDto.getPrice();
    this.stock = bookFormDto.getStock();
    this.mainCategory = bookFormDto.getMainCategory();
    this.midCategory = bookFormDto.getMidCategory();
    this.subCategory = bookFormDto.getSubCategory();
    this.detailCategory = bookFormDto.getDetailCategory();
    this.description = bookFormDto.getDescription();
  }
  	public void removeStock(int stockNumber) {
		int restStock = this.stock - stockNumber; // 10, 5 / 10, 20
		if (restStock < 0) {
			throw new OutOfStockException(
					"상품의 재고가 부족합니다.(현재 재고 수량: " + this.stock + ")");
		}
		this.stock = restStock; // 5
	}

	public void addStock(int stockNumber) {
		this.stock += stockNumber;
	}

}
