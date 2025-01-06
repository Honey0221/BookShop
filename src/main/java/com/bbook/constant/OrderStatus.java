package com.bbook.constant;

public enum OrderStatus {
    PAID("결제완료"),
    CANCEL("환불완료");

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
