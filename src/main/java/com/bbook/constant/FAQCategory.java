package com.bbook.constant;

public enum FAQCategory {
  GENERAL("일반"),
  DELIVERY("배송"),
  PAYMENT("결제"),
  REFUND("환불"),
  MEMBER("회원");

  private final String description;

  FAQCategory(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}