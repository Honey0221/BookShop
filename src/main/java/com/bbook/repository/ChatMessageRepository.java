package com.bbook.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbook.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findByRoomIdOrderByTimeAsc(String roomId);

  List<ChatMessage> findByRoomIdAndTimeBetween(String roomId, LocalDateTime start, LocalDateTime end);

  List<ChatMessage> findByRoomIdAndReadFalse(String roomId);

  long countByRoomIdAndReadFalse(String roomId);

  void deleteByRoomId(String roomId);
}
