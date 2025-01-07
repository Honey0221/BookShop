package com.bbook.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponseDto {
  private String message;
  private List<BookRecommendationDto> recommendations; // 책 추천 목록 (필요한 경우)
}
