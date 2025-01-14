package com.bbook.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.dto.ChatMessageDto;
import com.bbook.dto.ChatRoomDto;
import com.bbook.entity.ChatMessage;
import com.bbook.entity.ChatRoom;
import com.bbook.repository.ChatMessageRepository;
import com.bbook.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;

  // 활성 채팅방 관리를 위한 메모리 저장소
  private final Map<String, ChatRoomDto> activeChatRooms = new ConcurrentHashMap<>();

  // 채팅방 생성
  public ChatRoomDto createChatRoom(String userId) {
    // 이미 존재하는 사용자의 채팅방이 있는지 확인
    ChatRoom existingRoom = chatRoomRepository.findByUserIdAndStatus(userId, "ACTIVE");
    if (existingRoom != null) {
      return convertToChatRoomDto(existingRoom);
    }

    // 새 채팅방 생성
    ChatRoom chatRoom = ChatRoom.builder()
        .roomId(UUID.randomUUID().toString())
        .userId(userId)
        .createdAt(LocalDateTime.now())
        .status("ACTIVE")
        .build();

    chatRoomRepository.save(chatRoom);

    ChatRoomDto roomDto = convertToChatRoomDto(chatRoom);
    activeChatRooms.put(chatRoom.getRoomId(), roomDto);

    return roomDto;
  }

  // 메시지 저장
  public void saveMessage(ChatMessageDto messageDto) {
    ChatMessage message = ChatMessage.builder()
        .roomId(messageDto.getRoomId())
        .sender(messageDto.getSender())
        .message(messageDto.getMessage())
        .time(LocalDateTime.now())
        .read(false)
        .build();

    chatMessageRepository.save(message);

    // 채팅방의 마지막 메시지 업데이트
    updateLastMessage(messageDto.getRoomId(), messageDto.getMessage());
  }

  // 모든 채팅방 조회 (관리자용)
  public List<ChatRoomDto> getAllChatRooms() {
    List<ChatRoom> rooms = chatRoomRepository.findAllByOrderByCreatedAtDesc();
    return rooms.stream()
        .map(this::convertToChatRoomDto)
        .collect(Collectors.toList());
  }

  // 특정 사용자의 채팅방 조회
  public List<ChatRoomDto> getUserChatRooms(String userId) {
    List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);
    return rooms.stream()
        .map(this::convertToChatRoomDto)
        .collect(Collectors.toList());
  }

  // 채팅방 메시지 내역 조회
  public List<ChatMessageDto> getChatMessages(String roomId) {
    List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimeAsc(roomId);
    return messages.stream()
        .map(this::convertToChatMessageDto)
        .collect(Collectors.toList());
  }

  // 채팅방 상태 업데이트
  public void updateRoomStatus(String roomId) {
    ChatRoom room = chatRoomRepository.findByRoomId(roomId)
        .orElseThrow(() -> new RuntimeException("Chat room not found"));

    room.setLastReadTime(LocalDateTime.now());
    chatRoomRepository.save(room);
  }

  // 채팅방 삭제
  public void deleteRoom(String roomId) {
    chatRoomRepository.deleteByRoomId(roomId);
    activeChatRooms.remove(roomId);
  }

  // 채팅방 정보 조회
  public ChatRoomDto getChatRoom(String roomId) {
    ChatRoom room = chatRoomRepository.findByRoomId(roomId)
        .orElseThrow(() -> new RuntimeException("Chat room not found"));
    return convertToChatRoomDto(room);
  }

  // 채팅방의 마지막 메시지 업데이트
  private void updateLastMessage(String roomId, String message) {
    ChatRoom room = chatRoomRepository.findByRoomId(roomId)
        .orElseThrow(() -> new RuntimeException("Chat room not found"));

    room.setLastMessage(message);
    room.setLastMessageTime(LocalDateTime.now());
    chatRoomRepository.save(room);
  }

  // Entity -> DTO 변환 메서드들
  private ChatRoomDto convertToChatRoomDto(ChatRoom room) {
    return ChatRoomDto.builder()
        .roomId(room.getRoomId())
        .userId(room.getUserId())
        .lastMessage(room.getLastMessage())
        .lastMessageTime(room.getLastMessageTime())
        .createdAt(room.getCreatedAt())
        .status(room.getStatus())
        .build();
  }

  private ChatMessageDto convertToChatMessageDto(ChatMessage message) {
    return ChatMessageDto.builder()
        .roomId(message.getRoomId())
        .sender(message.getSender())
        .message(message.getMessage())
        .time(message.getTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
        .build();
  }
}
