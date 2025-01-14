package com.bbook.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.dto.ChatMessageDto;
import com.bbook.dto.ChatRoomDto;
import com.bbook.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;
  private final ChatService chatService;

  @GetMapping("/admin/members/chatMng")
  public String chatMng() {
    return "admin/members/chatMng";
  }

  // WebSocket 메시지 처리
  @MessageMapping("/chat.message")
  public void handleChatMessage(ChatMessageDto message) {

    message.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

    // 메시지 저장
    chatService.saveMessage(message);

    // 개인별 채팅방으로 메시지 전송
    messagingTemplate.convertAndSend("/queue/chat." + message.getRoomId(), message);

    // 관리자에게도 메시지 전송
    messagingTemplate.convertAndSend("/queue/admin.chat." + message.getRoomId(), message);
  }

  // 사용자용 채팅방 생성
  @PostMapping("/chat/room")
  @ResponseBody
  public ResponseEntity<ChatRoomDto> createRoom(@AuthenticationPrincipal Object principal) {
    String userId;

    // 소셜 로그인 사용자
    if (principal instanceof OAuth2User) {
      OAuth2User oauth2User = (OAuth2User) principal;
      Map<String, Object> attributes = oauth2User.getAttributes();

      // 소셜 로그인 제공자별로 이메일 가져오기
      if (attributes.containsKey("email")) { // Google
        userId = (String) attributes.get("email");
      } else if (attributes.containsKey("kakao_account")) { // Kakao
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        userId = (String) kakaoAccount.get("email");
      } else if (attributes.containsKey("response")) { // Naver
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        userId = (String) response.get("email");
      } else {
        throw new RuntimeException("Unsupported OAuth2 provider");
      }
    }
    // 일반 로그인 사용자
    else if (principal instanceof User) {
      User user = (User) principal;
      userId = user.getUsername();
    } else {
      throw new RuntimeException("Unknown authentication type");
    }

    ChatRoomDto room = chatService.createChatRoom(userId);
    return ResponseEntity.ok(room);
  }

  // 관리자용 채팅방 목록 조회
  @GetMapping("/admin/chat/rooms")
  @ResponseBody
  public ResponseEntity<List<ChatRoomDto>> getChatRooms() {
    List<ChatRoomDto> rooms = chatService.getAllChatRooms();
    return ResponseEntity.ok(rooms);
  }

  // 채팅방 메시지 내역 조회
  @GetMapping("/admin/chat/messages/{roomId}")
  @ResponseBody
  public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable String roomId) {
    List<ChatMessageDto> messages = chatService.getChatMessages(roomId);
    return ResponseEntity.ok(messages);
  }

  // 채팅방 상태 업데이트 (읽음 처리 등)
  @PostMapping("/admin/chat/room/{roomId}/status")
  @ResponseBody
  public ResponseEntity<Void> updateRoomStatus(@PathVariable String roomId) {
    chatService.updateRoomStatus(roomId);
    return ResponseEntity.ok().build();
  }

  // 채팅방 삭제 (필요한 경우)
  @PostMapping("/admin/chat/room/{roomId}/delete")
  @ResponseBody
  public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
    chatService.deleteRoom(roomId);
    return ResponseEntity.ok().build();
  }

  // 채팅방 정보 조회
  @GetMapping("/chat/room/{roomId}")
  @ResponseBody
  public ResponseEntity<ChatRoomDto> getRoomInfo(@PathVariable String roomId) {
    ChatRoomDto room = chatService.getChatRoom(roomId);
    return ResponseEntity.ok(room);
  }

  // 사용자의 채팅방 목록 조회
  @GetMapping("/chat/rooms/my")
  @ResponseBody
  public ResponseEntity<List<ChatRoomDto>> getMyRooms(@AuthenticationPrincipal User user) {
    List<ChatRoomDto> rooms = chatService.getUserChatRooms(user.getUsername());
    return ResponseEntity.ok(rooms);
  }

  // 사용자용 채팅 메시지 내역 조회
  @GetMapping("/chat/messages/{roomId}")
  @ResponseBody
  public ResponseEntity<List<ChatMessageDto>> getUserChatMessages(
      @PathVariable String roomId,
      @AuthenticationPrincipal Object principal) {
    String userId;
    // 소셜 로그인 사용자
    if (principal instanceof OAuth2User) {
      OAuth2User oauth2User = (OAuth2User) principal;
      Map<String, Object> attributes = oauth2User.getAttributes();

      // 소셜 로그인 제공자별로 이메일 가져오기
      if (attributes.containsKey("email")) { // Google
        userId = (String) attributes.get("email");
      } else if (attributes.containsKey("kakao_account")) { // Kakao
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        userId = (String) kakaoAccount.get("email");
      } else if (attributes.containsKey("response")) { // Naver
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        userId = (String) response.get("email");
      } else {
        throw new RuntimeException("Unsupported OAuth2 provider");
      }
    }
    // 일반 로그인 사용자
    else if (principal instanceof User) {
      User user = (User) principal;
      userId = user.getUsername();
    } else {
      throw new RuntimeException("Unknown authentication type");
    }
    // 해당 사용자의 채팅방인지 확인
    ChatRoomDto room = chatService.getChatRoom(roomId);
    if (room != null && room.getUserId().equals(userId)) {
      List<ChatMessageDto> messages = chatService.getChatMessages(roomId);
      return ResponseEntity.ok(messages);
    }

    return ResponseEntity.status(HttpStatus.SC_FORBIDDEN).build();
  }

  @MessageMapping("/admin/chat")
  public void handleAdminChatMessage(ChatMessageDto message) {
    message.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    message.setSender("ADMIN"); // 발신자를 ADMIN으로 설정

    // 메시지 저장
    chatService.saveMessage(message);

    // 사용자의 채팅방으로 메시지 전송
    messagingTemplate.convertAndSend("/queue/chat." + message.getRoomId(), message);

    // 관리자 채팅방으로도 메시지 전송
    messagingTemplate.convertAndSend("/queue/admin.chat." + message.getRoomId(), message);
  }
}
