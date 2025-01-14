package com.bbook.dto;

import com.bbook.constant.RequestPriority;
import com.bbook.constant.RequestStatus;
import com.bbook.entity.Request;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RequestFormDto {
  private Long id;
  private String email;
  private String title;
  private String content;
  private String answer;
  private RequestStatus status;
  private RequestPriority priority;
  private LocalDateTime createDate;

  // Entity -> DTO 변환
  public static RequestFormDto of(Request request) {
    RequestFormDto dto = new RequestFormDto();
    dto.setId(request.getId());
    dto.setEmail(request.getEmail());
    dto.setTitle(request.getTitle());
    dto.setContent(request.getContent());
    dto.setAnswer(request.getAnswer());
    dto.setStatus(request.getStatus());
    dto.setPriority(request.getPriority());
    dto.setCreateDate(request.getCreateDate());
    return dto;
  }
}
