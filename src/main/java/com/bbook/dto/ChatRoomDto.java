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
public class ChatRoomDto {
  private String roomId;
  private String userId;
  private String adminId;
  private String lastMessage;
  private LocalDateTime lastMessageTime;
  private LocalDateTime createdAt;
  private String status;

  @Builder
  public ChatRoomDto(String roomId, String userId, String adminId, String lastMessage,
      LocalDateTime lastMessageTime, LocalDateTime createdAt, String status) {
    this.roomId = roomId;
    this.userId = userId;
    this.adminId = adminId;
    this.lastMessage = lastMessage;
    this.lastMessageTime = lastMessageTime;
    this.createdAt = createdAt;
    this.status = status;
  }
}