package com.bbook.service;

import org.springframework.stereotype.Service;

import com.bbook.constant.RequestPriority;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestContentAnalyzerService {
  // 키워드 기반 우선순위 분석
  public RequestPriority analyzePriority(String content) {
    String lowerContent = content.toLowerCase();

    // 긴급 키워드
    if (containsUrgentKeywords(lowerContent)) {
      return RequestPriority.URGENT;
    }

    // 높은 우선순위 키워드
    if (containsHighPriorityKeywords(lowerContent)) {
      return RequestPriority.HIGH;
    }

    // 중간 우선순위 키워드
    if (containsMediumPriorityKeywords(lowerContent)) {
      return RequestPriority.MEDIUM;
    }

    // 기본 우선순위
    return RequestPriority.LOW;
  }

  private boolean containsUrgentKeywords(String content) {
    return content.contains("긴급") ||
        content.contains("급함") ||
        content.contains("즉시") ||
        content.contains("문제") ||
        content.contains("오류") ||
        content.contains("에러");
  }

  private boolean containsHighPriorityKeywords(String content) {
    return content.contains("중요") ||
        content.contains("필요") ||
        content.contains("요청") ||
        content.contains("환불") ||
        content.contains("취소");
  }

  private boolean containsMediumPriorityKeywords(String content) {
    return content.contains("확인") ||
        content.contains("문의") ||
        content.contains("질문") ||
        content.contains("방법");
  }
}
