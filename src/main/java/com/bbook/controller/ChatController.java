package com.bbook.controller;

import com.bbook.dto.ChatMessageRequestDto;
import com.bbook.dto.ChatMessageResponseDto;
import com.bbook.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping("/message")
  public ResponseEntity<ChatMessageResponseDto> processMessage(@RequestBody ChatMessageRequestDto request) {
    ChatMessageResponseDto response = chatService.processMessage(request);
    return ResponseEntity.ok(response);
  }
}
