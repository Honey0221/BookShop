package com.bbook.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.dto.ReviewCleanBotDto;
import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewReportDto;
import com.bbook.repository.ReviewRepository;
import com.bbook.service.ReviewAnalysisService;
import com.bbook.service.admin.AdminReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminReviewController {
	private final ReviewRepository reviewRepository;
	private final AdminReviewService adminReviewService;
	private final ReviewAnalysisService analysisService;

	@GetMapping("/reviewMng")
	public String reviewMng(@RequestParam(required = false) String searchType,
			@RequestParam(required = false) String keyword,
			@PageableDefault(size = 10) Pageable pageable, Model model) {
		// 리뷰 검색 결과 로드
		Page<ReviewDto> reviewPage;
		if (StringUtils.hasText(keyword)) {
			reviewPage = adminReviewService.searchReviews(searchType, keyword, pageable);
		} else {
			reviewPage = Page.empty(pageable);
		}
		model.addAttribute("reviews", reviewPage.getContent());
		model.addAttribute("reviewPagination", reviewPage);

		// 신고 리뷰 목록과 카운트
		Page<ReviewReportDto> reportsPage = adminReviewService.getAllReports(pageable);
		model.addAttribute("reports", reportsPage.getContent());
		model.addAttribute("reportPagination", reportsPage);
		model.addAttribute("reportCount", adminReviewService.getReportCount());

		model.addAttribute("searchType", searchType);
		model.addAttribute("keyword", keyword);

		return "/admin/reviews/reviewMng";
	}

	@GetMapping("/reviewCleanBot")
	public String reviewCleanBot(Model model,
			@PageableDefault(size = 10) Pageable pageable) {
		Page<ReviewCleanBotDto> reviewsPage
				= adminReviewService.getFlaggedReviewsForCleanBot(pageable);

		model.addAttribute("reviews", reviewsPage.getContent());
		model.addAttribute("pagination", reviewsPage);
		return "/admin/reviews/reviewCleanBot";
	}

	@PostMapping("/api/reviews/analyze")
	@ResponseBody
	public ResponseEntity<String> analyzeReviews() {
		try {
			analysisService.analyzeBookReviews(1L);
			return ResponseEntity.ok("분석이 완료되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("분석 중 오류 발생 : " + e.getMessage());
		}
	}

	@PostMapping("/api/reviews/batch-delete")
	@ResponseBody
	public ResponseEntity<String> batchDeleteReview(@RequestBody List<Long> reviewIds) {
		try {
			if (reviewIds == null || reviewIds.isEmpty()) {
				return ResponseEntity.badRequest().body("삭제할 리뷰가 선택되지 않았습니다.");
			}
			adminReviewService.deleteReviews(reviewIds);
			return ResponseEntity.ok("선택한 " + reviewIds.size() + "개의 리뷰가 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("리뷰 삭제 중 오류 발생 : " + e.getMessage());
		}
	}

	@PostMapping("/api/reports/{reportId}/process")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> processReport(
			@PathVariable Long reportId,
			@RequestParam String status) {
		try {
			Long reviewId = adminReviewService.processReport(reportId, status);
			Map<String, Object> response = new HashMap<>();
			response.put("message", "신고가 처리되었습니다.");
			response.put("reviewId", reviewId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "신고 처리 중 오류가 발생했습니다."
							+ e.getMessage()));
		}
	}
}
