package com.bbook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import com.bbook.constant.FAQCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "faq")
public class FAQEntity extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String question;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String answer;

  @Enumerated(EnumType.STRING)
  private FAQCategory category;

  @Column(nullable = false)
  private LocalDateTime createdAt;

}
