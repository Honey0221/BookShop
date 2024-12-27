package com.bbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer depth;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(nullable = false, length = 255)
  private String name;
}