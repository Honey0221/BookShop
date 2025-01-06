package com.bbook.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class Payment {
    private String impUid;
    private String merchantUid;
    private BigDecimal amount;
}