package com.bbook.controller;

import com.bbook.dto.AdminOrderDto;
import com.bbook.dto.OrderSearchDto;
import com.bbook.dto.PageResponseDto;
import com.bbook.entity.Order;
import com.bbook.service.AdminOrderService;
import com.bbook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderController orderController;
    private final OrderRepository orderRepository;

    @GetMapping("/orderMng")
    public String orderManagement() {
        return "admin/orderMng";
    }

    @GetMapping("/orderStat")
    public String orderStatistics() {
        return "admin/orderStat";
    }

    @GetMapping("/api/statistics/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            log.info("Fetching order summary for period: {} to {}", startDate, endDate);
            Map<String, Object> summary = adminOrderService.getOrderSummary(startDate, endDate);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error while fetching order summary", e);
            throw e;
        }
    }

    @GetMapping("/api/statistics/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            log.info("Fetching product statistics for period: {} to {}", startDate, endDate);
            Map<String, Object> productStats = adminOrderService.getProductStatistics(startDate, endDate);
            return ResponseEntity.ok(productStats);
        } catch (Exception e) {
            log.error("Error while fetching product statistics", e);
            throw e;
        }
    }

    @GetMapping("/api/statistics/subscription")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSubscriptionStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            log.info("Fetching subscription statistics for period: {} to {}", startDate, endDate);
            Map<String, Object> subscriptionStats = adminOrderService.getSubscriptionStatistics(startDate, endDate);
            return ResponseEntity.ok(subscriptionStats);
        } catch (Exception e) {
            log.error("Error while fetching subscription statistics", e);
            throw e;
        }
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<PageResponseDto<AdminOrderDto>> searchOrders(
            OrderSearchDto searchDto,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            log.info("Searching orders with criteria: {}", searchDto);
            Page<AdminOrderDto> orders = adminOrderService.searchOrders(searchDto, pageable);
            PageResponseDto<AdminOrderDto> response = new PageResponseDto<>(orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while searching orders", e);
            throw e;
        }
    }

    @GetMapping("/api/orders/{merchantUid}")
    @ResponseBody
    public ResponseEntity<AdminOrderDto> getOrderDetail(@PathVariable String merchantUid) {
        try {
            log.info("Fetching order detail for merchantUid: {}", merchantUid);
            AdminOrderDto order = adminOrderService.getOrderDetail(merchantUid);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error while fetching order detail", e);
            throw e;
        }
    }

    @PostMapping("/api/orders/{merchantUid}/refund")
    public ResponseEntity<Map<String, Object>> refundOrder(@PathVariable String merchantUid) {
        try {
            // 주문 상세 정보 조회
            AdminOrderDto orderDto = adminOrderService.getOrderDetail(merchantUid);
            if (orderDto == null) {
                throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
            }

            // 이미 취소된 주문인지 확인
            if (orderDto.getOrderStatus().equals("CANCEL")) {
                throw new IllegalStateException("이미 취소된 주문입니다.");
            }

            // 주문 ID 조회
            Order order = orderRepository.findByMerchantUid(merchantUid)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

            // 주문 취소 요청
            List<Long> orderIds = Collections.singletonList(order.getId());
            return orderController.cancelOrders(orderIds);

        } catch (Exception e) {
            log.error("Order refund failed for merchantUid: " + merchantUid, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}