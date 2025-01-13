package com.bbook.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

import com.bbook.entity.Member;
import com.bbook.repository.MemberRepository;
import com.bbook.service.EmailService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

	private final MemberRepository memberRepository;
	private final EmailService emailService;

	@GetMapping("/memberMng")
	public String memberList(@RequestParam(required = false) String searchType,
			@RequestParam(required = false) String searchKeyword,
			@RequestParam(defaultValue = "0") int page,
			Model model) {

		Pageable pageable = PageRequest.of(page, 10); // 한 페이지당 10개씩 표시
		Page<Member> memberPage;

		// 검색 조건이 있는 경우
		if (searchKeyword != null && !searchKeyword.isEmpty()) {
			if ("email".equals(searchType)) {
				memberPage = memberRepository.findByEmailContaining(searchKeyword, pageable);
			} else if ("nickname".equals(searchType)) {
				memberPage = memberRepository.findByNicknameContaining(searchKeyword, pageable);
			} else {
				memberPage = memberRepository.findAll(pageable);
			}
		} else {
			memberPage = memberRepository.findAll(pageable);
		}

		model.addAttribute("members", memberPage.getContent());
		model.addAttribute("pages", memberPage.getTotalPages());
		model.addAttribute("currentPage", memberPage.getNumber());

		return "admin/members/memberMng";
	}

	@GetMapping("/memberStat")
	public String memberStatistic() {
		return "admin/members/memberStat";
	}

	@PostMapping("/send-email")
	@ResponseBody
	public ResponseEntity<Map<String, String>> sendEmail(@RequestBody Map<String, String> emailRequest) {
		try {
			emailService.sendEmail(
					emailRequest.get("email"),
					emailRequest.get("subject"),
					emailRequest.get("content"));
			return ResponseEntity.ok(Map.of("message", "이메일이 성공적으로 발송되었습니다."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", "이메일 발송에 실패했습니다."));
		}
	}
}
