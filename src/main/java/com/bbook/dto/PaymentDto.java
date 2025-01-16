package com.bbook.dto;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PaymentDto {
    @JsonProperty("imp_uid")
    private String impUid;
    @JsonProperty("merchant_uid")
    private String merchantUid;
    private Long amount;
    private String status;

    @Override
    public String toString() {
        return "Payment{" +
                "impUid='" + impUid + '\'' +
                ", merchantUid='" + merchantUid + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}