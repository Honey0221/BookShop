package com.bbook.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.constant.ActivityType;
import com.bbook.service.MemberActivityService;
import com.bbook.service.MemberService;
import com.bbook.service.WishBookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wish")
public class WishBookController {
	private final MemberService memberService;
	private final WishBookService wishBookService;
	private final MemberActivityService memberActivityService;

	@PostMapping("/{bookId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleWish(
			@PathVariable(name = "bookId") Long bookId,
			@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails.getUsername();
		Long memberId = memberService.getMemberIdByEmail(email);
		boolean isWished = wishBookService.toggleWish(memberId, bookId);
		if (isWished) {
			memberActivityService.saveActivity(email, bookId, ActivityType.HEART);
		} else {
			memberActivityService.cancelActivity(email, bookId, ActivityType.HEART);
		}
		Map<String, Object> response = new HashMap<>();
		response.put("isWished", isWished);

		return ResponseEntity.ok(response);
	}
}
