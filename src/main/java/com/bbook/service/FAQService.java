package com.bbook.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.bbook.entity.FAQEntity;
import com.bbook.dto.FAQDto;
import com.bbook.repository.FAQRepository;

import lombok.RequiredArgsConstructor;

import com.bbook.constant.FAQCategory;

@Service
@RequiredArgsConstructor
public class FAQService {
  private final FAQRepository faqRepository;

  public List<FAQDto> getAllFAQs() {
    return faqRepository.findAllByOrderByIdDesc()
        .stream()
        .map(FAQDto::fromEntity)
        .collect(Collectors.toList());
  }

  public List<FAQDto> getFAQsByCategory(FAQCategory category) {
    return faqRepository.findByCategoryOrderByIdDesc(category)
        .stream()
        .map(FAQDto::fromEntity)
        .collect(Collectors.toList());
  }

  public void saveFAQ(FAQDto faqDto) {
    FAQEntity entity = new FAQEntity();
    entity.setQuestion(faqDto.getQuestion());
    entity.setAnswer(faqDto.getAnswer());
    entity.setCategory(faqDto.getCategory());
    entity.setCreatedAt(LocalDateTime.now());
    faqRepository.save(entity);
  }

  public void updateFAQ(FAQDto faqDto) {
    FAQEntity entity = faqRepository.findById(faqDto.getId())
        .orElseThrow(() -> new RuntimeException("FAQ not found"));

    entity.setQuestion(faqDto.getQuestion());
    entity.setAnswer(faqDto.getAnswer());
    entity.setCategory(faqDto.getCategory());
    entity.setCreatedAt(LocalDateTime.now());
    faqRepository.save(entity);
  }

  public void deleteFAQ(Long id) {
    faqRepository.deleteById(id);
  }
}
