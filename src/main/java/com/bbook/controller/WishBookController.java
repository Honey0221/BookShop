package com.bbook.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.service.MemberService;
import com.bbook.service.WishBookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wish")
public class WishBookController {
	private final MemberService memberService;
	private final WishBookService wishBookService;

	@PostMapping("/{bookId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleWish(
			@PathVariable(name = "bookId") Long bookId,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		try {
			String email = userDetails.getUsername();
			Long memberId = memberService.getMemberIdByEmail(email);
			System.out.println("memberId" + memberId);
			boolean isWished = wishBookService.toggleWish(memberId, bookId);

			Map<String, Object> response = new HashMap<>();
			System.out.println("111111111111");
			response.put("isWished", isWished);
			System.out.println("22222222");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
