package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.BookStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Getter
@Setter
@Table(name = "books")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Column(nullable = false)
  private String publisher;

  @Column(nullable = false)
  private int price;

  @Column
  private Integer stock;

  @Column(length = 2550, nullable = false)
  private String description;

  @Column(nullable = false)
  private String imageUrl;

  @Enumerated
  private BookStatus bookStatus;

  private LocalDateTime createdAt;

}
