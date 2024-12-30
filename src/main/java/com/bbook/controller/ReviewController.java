package com.bbook.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewRequestDto;
import com.bbook.entity.Member;
import com.bbook.service.MemberService;
import com.bbook.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final MemberService memberService;

	@PostMapping
	@ResponseBody
	public ResponseEntity<Map<String, Boolean>> createReview(
			@RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			System.out.println("BookId: " + request.getBookId());
			System.out.println("Rating: " + request.getRating());
			System.out.println("Content: " + request.getContent());

			String email = userDetails.getUsername();
			Long memberId = memberService.getMemberIdByEmail(email);

			reviewService.saveReview(
					memberId,
					request.getBookId(),
					request.getRating(),
					request.getContent());
			return ResponseEntity.ok(Map.of("success", true));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("에러 발생 : " + e.getMessage());
			return ResponseEntity.badRequest().body(Map.of("success", false));
		}
	}

	@GetMapping("/{bookId}")
	@ResponseBody
	public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable("bookId") Long bookId) {
		List<ReviewDto> reviews = reviewService.getBookReviews(bookId);
		return ResponseEntity.ok(reviews);
	}
}
