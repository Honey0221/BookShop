package com.bbook.dto;

import com.bbook.constant.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchDto {
    private String searchType; // 검색 유형 (merchantUid, memberName, email, orderName)
    private String searchKeyword; // 검색어
    private String orderStatus; // 주문 상태 (String으로 받아서 OrderStatus로 변환)
    private String startDate; // 시작일
    private String endDate; // 종료일

    public OrderStatus getOrderStatusEnum() {
        if (orderStatus == null || orderStatus.isEmpty()) {
            return null;
        }
        return OrderStatus.valueOf(orderStatus);
    }

    public LocalDateTime getStartDateTime() {
        if (startDate == null || startDate.isEmpty()) {
            return null;
        }
        return LocalDate.parse(startDate).atStartOfDay();
    }

    public LocalDateTime getEndDateTime() {
        if (endDate == null || endDate.isEmpty()) {
            return null;
        }
        return LocalDate.parse(endDate).atTime(LocalTime.MAX);
    }
}