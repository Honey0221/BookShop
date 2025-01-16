package com.bbook.controller.admin;

import com.bbook.dto.OrderStatisticsDto;
import com.bbook.service.OrderStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
public class OrderStatisticsController {

    private final OrderStatisticsService orderStatisticsService;

    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsDto> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("Fetching order statistics for period: {} to {}", startDate, endDate);

        // 날짜가 지정되지 않은 경우 최근 30일로 설정
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        OrderStatisticsDto statistics = orderStatisticsService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
}