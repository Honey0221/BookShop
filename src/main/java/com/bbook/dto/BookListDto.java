package com.bbook.dto;

import com.bbook.constant.BookStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BookListDto {
  private Long id; // 도서 ID
  private String title; // 도서 제목
  private String author; // 저자
  private String publisher; // 출판사
  private Integer price; // 가격
  private Integer stock; // 재고
  private String imageUrl; // 이미지 URL
  private String mainCategory; // 메인 카테고리
  private String midCategory; // 중분류 카테고리
  private String subCategory; // 서브 카테고리
  private String detailCategory; // 상세 카테고리
  private BookStatus bookStatus; // 도서 상태
  private LocalDateTime createdAt;// 등록 시간
  private String description; // 도서 설명
  private Long viewCount; // 조회수 필드 추가

  @Builder
  public BookListDto(Long id, String title, String author, String publisher,
      Integer price, Integer stock, String imageUrl,
      String mainCategory, String midCategory,
      String subCategory, String detailCategory,
      BookStatus bookStatus, LocalDateTime createdAt,
      String description, Long viewCount) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.publisher = publisher;
    this.price = price;
    this.stock = stock;
    this.imageUrl = imageUrl;
    this.mainCategory = mainCategory;
    this.midCategory = midCategory;
    this.subCategory = subCategory;
    this.detailCategory = detailCategory;
    this.bookStatus = bookStatus;
    this.createdAt = createdAt;
    this.description = description;
    this.viewCount = viewCount;
  }

  // 신상품 여부 확인 메서드 (1주일 이내 등록된 도서)
  public boolean isNew() {
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
    return createdAt.isAfter(oneWeekAgo);
  }

  // 품절 여부 확인 메서드
  public boolean isOutOfStock() {
    return stock != null && stock <= 0;
  }

  // 판매 가능 여부 확인 메서드
  public boolean isAvailable() {
    return bookStatus == BookStatus.SELL && !isOutOfStock();
  }
}
