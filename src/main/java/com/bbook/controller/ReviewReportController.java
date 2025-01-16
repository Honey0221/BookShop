package com.bbook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.entity.ReviewReport;
import com.bbook.service.ReviewReportService;
import com.bbook.service.admin.TelegramAlertService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class ReviewReportController {
	private final ReviewReportService reviewReportService;
	private final TelegramAlertService telegramAlertService;

	@GetMapping("/test-alert")
	@ResponseBody
	public ResponseEntity<String> testAlert() {
		telegramAlertService.sendTestMessage();
		return ResponseEntity.ok("텔레그램 테스트 알림 발송");
	}

	// 특정 리뷰의 신고 횟수 조회
	@GetMapping("/{reviewId}/count")
	@ResponseBody
	public ResponseEntity<Integer> getReportCount(@PathVariable Long reviewId) {
		int count = reviewReportService.getReportCount(reviewId);
		System.out.println("리뷰 ID : " + reviewId + "신고 횟수 : " + count);
		return ResponseEntity.ok(count);
	}

	// 특정 리뷰의 신고 목록 조회
	@GetMapping("/{reviewId}")
	@ResponseBody
	public ResponseEntity<List<ReviewReport>> getReportReport(@PathVariable Long reviewId) {
		List<ReviewReport> reports = reviewReportService.getReviewReports(reviewId);
		System.out.println("리뷰 ID : " + reviewId + "신고 건수 : " + reports.size());
		return ResponseEntity.ok(reports);
	}
}
