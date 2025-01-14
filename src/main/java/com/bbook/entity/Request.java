package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.RequestPriority;
import com.bbook.constant.RequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "requests")
public class Request extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestPriority priority;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String email; // Member 엔티티 대신 이메일 정보만 저장

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(columnDefinition = "TEXT")
  private String answer;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestStatus status = RequestStatus.WAITING;

  @Column(nullable = false)
  private LocalDateTime createDate;

  // 문의 생성을 위한 정적 팩토리 메서드
  public static Request createRequest(String email, String title, String content) {
    Request request = new Request();
    request.setPriority(RequestPriority.LOW);
    request.setEmail(email);
    request.setTitle(title);
    request.setContent(content);
    request.setStatus(RequestStatus.WAITING);
    request.setCreateDate(LocalDateTime.now());
    return request;
  }

  // 답변 등록 메서드
  public void addAnswer(String answer) {
    this.answer = answer;
    this.status = RequestStatus.ANSWERED;
  }
}
