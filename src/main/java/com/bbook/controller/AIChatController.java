package com.bbook.controller;

import com.bbook.dto.ChatMessageRequestDto;
import com.bbook.dto.ChatMessageResponseDto;
import com.bbook.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

  private final AIChatService aiChatService;

  @PostMapping("/message")
  public ResponseEntity<ChatMessageResponseDto> processMessage(@RequestBody ChatMessageRequestDto request) {
    ChatMessageResponseDto response = aiChatService.processMessage(request);
    return ResponseEntity.ok(response);
  }
}
