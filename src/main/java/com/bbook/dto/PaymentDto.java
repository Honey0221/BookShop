package com.bbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto {
    private String impUid; // 아임포트 결제 고유 번호
    private String merchantUid; // 주문 번호
    private Long amount; // 결제 금액
    private String status; // 결제 상태
}