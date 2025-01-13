package com.bbook.service;

import com.bbook.dto.ChatMessageRequestDto;
import com.bbook.dto.ChatMessageResponseDto;
import com.bbook.dto.BookRecommendationDto;
import com.bbook.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIChatService {

  private final BookRepository bookRepository;

  public ChatMessageResponseDto processMessage(ChatMessageRequestDto request) {
    String userMessage = request.getMessage().toLowerCase();

    // 카테고리별 추천 로직
    String category = null;
    String responseMessage = null;

    if (userMessage.contains("소설")) {
      category = "소설";
      responseMessage = "소설 분야에서 인기 있는 책들을 찾아보겠습니다.";
    } else if (userMessage.contains("자기계발")) {
      category = "자기계발";
      responseMessage = "자기계발 분야의 인기 도서를 추천해드립니다.";
    } else if (userMessage.contains("과학")) {
      category = "과학";
      responseMessage = "과학 분야의 베스트셀러를 소개해드립니다.";
    } else if (userMessage.contains("기술") || userMessage.contains("공학")) {
      category = "기술/공학";
      responseMessage = "기술/공학 분야의 추천 도서입니다.";
    }

    // 카테고리가 매칭된 경우 추천 도서 반환
    if (category != null) {
      return ChatMessageResponseDto.builder()
          .message(responseMessage)
          .recommendations(bookRepository.findTopBooksByCategory(category, 3)
              .stream()
              .map(book -> BookRecommendationDto.from(book, null))
              .toList())
          .build();
    }

    // 기본 응답
    return ChatMessageResponseDto.builder()
        .message("죄송합니다. 좀 더 구체적으로 말씀해 주시겠어요? 예를 들어 '소설 추천해줘' 또는 'IT 책 추천해줘'와 같이 말씀해 주시면 도움이 될 것 같습니다.")
        .build();
  }
}
