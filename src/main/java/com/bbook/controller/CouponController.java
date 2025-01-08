package com.bbook.controller;

import com.bbook.entity.Coupon;
import com.bbook.entity.Member;
import com.bbook.repository.MemberRepository;
import com.bbook.service.CouponService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class CouponController {

    private final MemberRepository memberRepository;
    private final CouponService couponService;

    @GetMapping("/my-coupons")
    public String myCoupons(Model model, Principal principal) {
        try {
            String email = principal.getName();
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

            List<Coupon> availableCoupons = couponService.getAvailableCoupons(member);
            model.addAttribute("availableCoupons", availableCoupons);

            return "member/my-coupons";
        } catch (Exception e) {
            log.error("쿠폰 페이지 로딩 중 오류 발생: {}", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/coupon-zone")
    public String couponZone(Model model, Principal principal) {
        try {
            if (principal != null) {
                String email = principal.getName();
                Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
                List<Coupon> availableCoupons = couponService.getAvailableCoupons(member);
                model.addAttribute("availableCoupons", availableCoupons);
            }
            return "member/coupon-zone";
        } catch (Exception e) {
            log.error("쿠폰존 페이지 로딩 중 오류 발생: {}", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/check-coupon-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkCouponStatus(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("hasDownloaded", false);
                return ResponseEntity.ok(response);
            }

            String email = principal.getName();
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

            boolean hasDownloaded = couponService.hasDownloadedCoupon(member);
            response.put("hasDownloaded", hasDownloaded);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("hasDownloaded", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/download-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downloadCoupon(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }

            String email = principal.getName();
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

            couponService.downloadCoupon(member);

            response.put("success", true);
            response.put("message", "쿠폰이 발급되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // 비즈니스 로직 관련 예외 (최대 보유 개수 초과 등)
            log.warn("쿠폰 다운로드 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 기타 예외
            log.error("쿠폰 다운로드 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "쿠폰 발급 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.ok(response);
        }
    }
}
