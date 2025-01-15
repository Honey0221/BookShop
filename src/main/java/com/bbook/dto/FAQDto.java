package com.bbook.dto;

import java.time.LocalDateTime;

import com.bbook.constant.FAQCategory;
import com.bbook.entity.FAQEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class FAQDto {
  private Long id;
  private String question;
  private String answer;
  private FAQCategory category;
  private LocalDateTime createdAt;

  public static FAQDto fromEntity(FAQEntity entity) {
    FAQDto dto = new FAQDto();
    dto.setId(entity.getId());
    dto.setQuestion(entity.getQuestion());
    dto.setAnswer(entity.getAnswer());
    dto.setCategory(entity.getCategory());
    dto.setCreatedAt(entity.getCreatedAt());
    return dto;
  }
}
