package com.bbook.dto;

import com.bbook.entity.Book;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRecommendationDto {
  private Long bookId;
  private String title;
  private String author;
  private String imageUrl;
  private String mainCategory;
  private String midCategory;
  private String detailCategory;
  private Integer price;
  private Double score; // 추천 점수

  @Builder
  public BookRecommendationDto(Long bookId, String title, String author,
      String imageUrl, String mainCategory,
      String midCategory, String detailCategory,
      Integer price, Double score) {
    this.bookId = bookId;
    this.title = title;
    this.author = author;
    this.imageUrl = imageUrl;
    this.mainCategory = mainCategory;
    this.midCategory = midCategory;
    this.detailCategory = detailCategory;
    this.price = price;
    this.score = score;
  }

  // Book 엔티티로부터 DTO 생성
  public static BookRecommendationDto from(Book book, Double score) {
    return BookRecommendationDto.builder()
        .bookId(book.getId())
        .title(book.getTitle())
        .author(book.getAuthor())
        .imageUrl(book.getImageUrl())
        .mainCategory(book.getMainCategory())
        .midCategory(book.getMidCategory())
        .detailCategory(book.getDetailCategory())
        .price(book.getPrice())
        .score(score)
        .build();
  }
}