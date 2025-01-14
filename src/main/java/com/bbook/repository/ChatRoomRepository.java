package com.bbook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbook.entity.ChatRoom;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  ChatRoom findByUserIdAndStatus(String userId, String status);

  List<ChatRoom> findByUserId(String userId);

  Optional<ChatRoom> findByRoomId(String roomId);

  List<ChatRoom> findAllByOrderByCreatedAtDesc();

  void deleteByRoomId(String roomId);

  List<ChatRoom> findByActiveTrue();

  boolean existsByRoomId(String roomId);
}
