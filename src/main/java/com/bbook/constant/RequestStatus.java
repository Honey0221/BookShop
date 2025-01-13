package com.bbook.constant;

public enum RequestStatus {
  WAITING("답변대기"),
  ANSWERED("답변완료");

  private final String displayValue;

  RequestStatus(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}