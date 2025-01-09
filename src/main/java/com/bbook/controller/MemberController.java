package com.bbook.controller;

import com.bbook.dto.MemberNicknameDto;
import com.bbook.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bbook.dto.MemberSignUpDto;
import com.bbook.config.MemberDetails;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final JavaMailSender mailSender;
    private final HttpSession session;

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @GetMapping("/login/error")
    @ResponseBody
    public ResponseEntity<Map<String, String>> loginError() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "아이디 또는 비밀번호를 확인해주세요.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @GetMapping("/login-error")
    public String loginError(Model model, HttpServletRequest request) {
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage == null) {
            errorMessage = "아이디 또는 비밀번호를 확인해주세요.";
        }
        model.addAttribute("loginErrorMsg", errorMessage);
        return "member/login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberSignUpDto", new MemberSignUpDto());
        return "member/signupForm";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signUp(@Valid @ModelAttribute MemberSignUpDto signUpDto,
            BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            // 유효성 검사 에러 처리
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            response.put("status", "error");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            memberService.signUp(signUpDto);
            response.put("status", "success");
            response.put("redirectUrl", "/members/login");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/social/nickname")
    public String socialNicknameForm(Model model) {
        log.info("소셜 회원 닉네임 설정 폼 요청");
        model.addAttribute("memberNicknameDto", new MemberNicknameDto());
        return "member/socialNicknameForm";
    }

    @PostMapping("/social/nickname")
    public String setSocialNickname(@Valid @ModelAttribute MemberNicknameDto memberNicknameDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        if (bindingResult.hasErrors()) {
            return "member/socialNicknameForm";
        }

        try {
            memberService.setNickname(memberDetails.getMember().getId(),
                    memberNicknameDto.getNickname());
            return "redirect:/";
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("nickname", "error.nickname", e.getMessage());
            return "member/socialNicknameForm";
        }
    }

    @PostMapping("/emailCheck")
    @ResponseBody
    public ResponseEntity<String> emailCheck(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // 이메일 중복 체크
        if (memberService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }

        // 인증번호 생성 (6자리)
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 이메일 내용 설정
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("회원가입 인증번호");
        message.setText("인증번호: " + code);

        try {
            mailSender.send(message);
            // 인증번호를 세션에 저장
            session.setAttribute("emailVerificationCode", code);
            session.setAttribute("emailVerificationEmail", email);
            return ResponseEntity.ok("이메일로 인증번호가 발송되었습니다.");
        } catch (MailException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 전송에 실패했습니다.");
        }
    }

    // 인증번호 확인을 위한 새로운 엔드포인트
    @PostMapping("/verifyEmail")
    @ResponseBody
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        String inputCode = request.get("code");
        String email = request.get("email");
        String storedCode = (String) session.getAttribute("emailVerificationCode");
        String storedEmail = (String) session.getAttribute("emailVerificationEmail");

        if (storedCode == null || storedEmail == null) {
            return ResponseEntity.badRequest().body("인증번호가 만료되었습니다. 다시 시도해주세요.");
        }

        if (!email.equals(storedEmail)) {
            return ResponseEntity.badRequest().body("이메일이 일치하지 않습니다.");
        }

        if (inputCode.equals(storedCode)) {
            session.removeAttribute("emailVerificationCode");
            session.removeAttribute("emailVerificationEmail");
            return ResponseEntity.ok("인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
        }
    }
}
