package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.BookStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
  @Enumerated(EnumType.ORDINAL) // TINYINT 타입이므로 ORDINAL 사용
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

  @Column(columnDefinition = "LONGTEXT")
  private String description;
}
