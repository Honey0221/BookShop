package com.bbook.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.constant.ActivityType;
import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewRequestDto;
import com.bbook.dto.ReviewUpdateDto;
import com.bbook.service.MemberActivityService;
import com.bbook.service.MemberService;
import com.bbook.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final MemberService memberService;
	private final MemberActivityService memberActivityService;

	@PostMapping
	@ResponseBody
	public ResponseEntity<Map<String, Boolean>> createReview(
			@RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			String email = userDetails.getUsername();
			Long memberId = memberService.getMemberIdByEmail(email);

			// 리뷰 활동 기록 저장 코드 영역
			if (email != null) {
				memberActivityService
						.saveActivity(email, request.getBookId(), ActivityType.REVIEW);
			}

			ReviewDto reviewDto = ReviewDto.builder()
							.memberId(memberId)
							.bookId(request.getBookId())
							.rating(request.getRating())
							.content(request.getContent())
							.build();

			reviewService.saveReview(reviewDto);
			return ResponseEntity.ok(Map.of("success", true));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("에러 발생 : " + e.getMessage());
			return ResponseEntity.badRequest().body(Map.of("success", false));
		}
	}

	@GetMapping("/{bookId}")
	@ResponseBody
	public ResponseEntity<Page<ReviewDto>> getReviews(
			@PathVariable("bookId") Long bookId,
			@RequestParam(defaultValue = "0") int page,
			@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails != null ? userDetails.getUsername() : null;
		Long currentMemberId = email != null ?
				memberService.getMemberIdByEmail(email) : null;

		PageRequest pageRequest = PageRequest.of(
				page, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<ReviewDto> reviews =
				reviewService.getBookReviews(bookId, currentMemberId, pageRequest);

		return ResponseEntity.ok(reviews);
	}

	@GetMapping("/average/{bookId}")
	@ResponseBody
	public Double getAvgRating(@PathVariable("bookId") Long bookId) {
		return reviewService.getAverageRatingByBookId(bookId);
	}

	@PatchMapping("/{reviewId}")
	@ResponseBody
	public ResponseEntity<Map<String, Boolean>> updateReview(
			@PathVariable("reviewId") Long reviewId,
			@RequestBody ReviewUpdateDto updateDto,
			@AuthenticationPrincipal UserDetails userDetails) {
		System.out.println("리뷰 수정 요청 아이디: " + reviewId);
		System.out.println("수정 내용 - rating : " + updateDto.getRating());
		System.out.println("수정 내용 - content : " + updateDto.getContent());

		try {
			String email = userDetails.getUsername();
			Long memberId = memberService.getMemberIdByEmail(email);

			reviewService.updateReview(reviewId, memberId, updateDto);
			return ResponseEntity.ok(Map.of("success", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false));
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
}
