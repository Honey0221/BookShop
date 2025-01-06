package com.bbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundAccountRequest {
    private String bankCode; // 은행 코드
    private String bankNum; // 계좌번호
    private String holderName; // 예금주명
}