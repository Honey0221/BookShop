package com.bbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestDto {
  private String message;
  private String userId;
}
