package com.bbook.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.constant.ActivityType;
import com.bbook.constant.ReportType;
import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewRequestDto;
import com.bbook.dto.ReviewStatsDto;
import com.bbook.dto.ReviewUpdateDto;
import com.bbook.service.MemberActivityService;
import com.bbook.service.MemberService;
import com.bbook.service.OrderService;
import com.bbook.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final OrderService orderService;
	private final MemberService memberService;
	private final MemberActivityService memberActivityService;

	@PostMapping
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createReview(
			@ModelAttribute ReviewRequestDto request,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			String email = userDetails.getUsername();
			Long memberId = memberService.getMemberIdByEmail(email);

			boolean hasPurchased =
					orderService.hasUserPurchasedBook(memberId, request.getBookId());

			if (!hasPurchased) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("success", false));
			}

			System.out.println("받은 리뷰 데이터 - bookId: " + request.getBookId());
			System.out.println("받은 리뷰 데이터 - rating: " + request.getRating());
			System.out.println("받은 리뷰 데이터 - content: " + request.getContent());
			System.out.println("받은 리뷰 데이터 - tagType: " + request.getTagType());

			ReviewDto reviewDto = ReviewDto.builder()
							.memberId(memberId)
							.bookId(request.getBookId())
							.rating(request.getRating())
							.content(request.getContent())
							.reviewImages(request.getReviewImages())
							.tagType(request.getTagType())
							.build();

			reviewService.createReview(reviewDto);

			// 리뷰 활동 기록 저장 코드 영역
			if (email != null) {
				memberActivityService
						.saveActivity(email, request.getBookId(), ActivityType.REVIEW);
			}

			// 통계 데이터 업데이트
			ReviewStatsDto updatedStats = reviewService.getReviewStats(request.getBookId());
			Double updatedAvgRating = reviewService.getAverageRatingByBookId(request.getBookId());

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("stats", updatedStats);
			response.put("avgRating", updatedAvgRating);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.out.println("에러 발생 : " + e.getMessage());
			return ResponseEntity.badRequest().body(Map.of("success", false,
					"error", e.getMessage()));
		}
	}

	@GetMapping("/{bookId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getReviews(
			@PathVariable("bookId") Long bookId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "likes") String sort,
			@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails != null ? userDetails.getUsername() : null;
		Long currentMemberId = email != null ?
				memberService.getMemberIdByEmail(email) : null;

		PageRequest pageRequest = PageRequest.of(
				page, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<ReviewDto> reviews =
				reviewService.getBookReviews(bookId, currentMemberId, pageRequest, sort);

		// 통계 데이터 조회
		ReviewStatsDto stats = reviewService.getReviewStats(bookId);
		Double avgRating = reviewService.getAverageRatingByBookId(bookId);

		// 응답 데이터 구성
		Map<String, Object> response = new HashMap<>();
		response.put("content", reviews.getContent());
		response.put("totalPages", reviews.getTotalPages());
		response.put("totalElements", reviews.getTotalElements());
		response.put("size", reviews.getSize());
		response.put("number", reviews.getNumber());
		response.put("first", reviews.isFirst());
		response.put("last", reviews.isLast());
		response.put("empty", reviews.isEmpty());
		response.put("stats", stats);
		response.put("avgRating", avgRating);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/average/{bookId}")
	@ResponseBody
	public ResponseEntity<Double> getAverageRating(@PathVariable("bookId") Long bookId) {
		try {
			Double avgRating = reviewService.getAverageRatingByBookId(bookId);
			System.out.println("Average rating: " + avgRating); // 디버깅용
			return ResponseEntity.ok(avgRating);
		} catch (Exception e) {
			System.out.println("Error getting average: " + e.getMessage()); // 디버깅용
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PatchMapping("/{reviewId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateReview(
			@PathVariable("reviewId") Long reviewId,
			@ModelAttribute ReviewUpdateDto updateDto,
			@AuthenticationPrincipal UserDetails userDetails) {
		System.out.println("리뷰 수정 요청 아이디: " + reviewId);
		System.out.println("수정 내용 - rating : " + updateDto.getRating());
		System.out.println("수정 내용 - content : " + updateDto.getContent());
		System.out.println("수정 내용 - tagType : " + updateDto.getTagType());

		try {
			String email = userDetails.getUsername();
			Long memberId = memberService.getMemberIdByEmail(email);

			reviewService.updateReview(reviewId, memberId, updateDto);

			Long bookId = reviewService.getBookIdByReviewId(reviewId);
			ReviewStatsDto updatedStats = reviewService.getReviewStats(bookId);
			Double updatedAvgRating = reviewService.getAverageRatingByBookId(bookId);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("stats", updatedStats);
			response.put("avgRating", updatedAvgRating);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of(
					"success", false, "error", e.getMessage()));
		}
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") Long reviewId) {
		reviewService.deleteReview(reviewId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/count/{bookId}")
	@ResponseBody
	public ResponseEntity<Long> getReviewCount(@PathVariable("bookId") Long bookId) {
		long count = reviewService.getReviewCount(bookId);
		return ResponseEntity.ok(count);
	}

	@PostMapping("/{reviewId}/like")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long reviewId,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String email = userDetails.getUsername();
		Long memberId = memberService.getMemberIdByEmail(email);

		try {
			Map<String, Object> result = reviewService.toggleLike(reviewId, memberId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/stats/{bookId}")
	@ResponseBody
	public ResponseEntity<ReviewStatsDto> getReviewStats(
			@PathVariable("bookId") Long bookId) {
		try {
			ReviewStatsDto stats = reviewService.getReviewStats(bookId);
			System.out.println("Stats response: " + stats); // 디버깅용
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			System.out.println("Error getting stats: " + e.getMessage()); // 디버깅용
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/report")
	public ResponseEntity<Void> checkLoginForReport(
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("/report")
	public ResponseEntity<Void> reportReview(
			@RequestParam Long reviewId, @RequestParam ReportType reportType,
			@RequestParam String content,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String email = userDetails.getUsername();
		Long memberId = memberService.getMemberIdByEmail(email);

		try {
			reviewService.reportReview(reviewId, memberId, reportType, content);
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
