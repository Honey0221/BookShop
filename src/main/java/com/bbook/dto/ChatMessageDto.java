package com.bbook.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class ChatMessageDto {
  private String type; // MESSAGE, ENTER, LEAVE
  private String roomId;
  private String sender;
  private String message;
  private String time;
  private LocalDateTime lastMessageTime;
  private String userId;

  @Builder
  public ChatMessageDto(String type, String roomId, String sender, String message, String time,
      LocalDateTime lastMessageTime, String userId) {
    this.type = type;
    this.roomId = roomId;
    this.sender = sender;
    this.message = message;
    this.time = time;
    this.lastMessageTime = lastMessageTime;
    this.userId = userId;
  }
}