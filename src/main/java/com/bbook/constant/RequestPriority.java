package com.bbook.constant;

import lombok.Getter;

@Getter
public enum RequestPriority {
  URGENT("긴급", 3),
  HIGH("높음", 2),
  MEDIUM("중간", 1),
  LOW("낮음", 0);

  private final String description;
  private final int value;

  RequestPriority(String description, int value) {
    this.description = description;
    this.value = value;
  }
}
