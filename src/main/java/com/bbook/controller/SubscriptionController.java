package com.bbook.controller;

import com.bbook.service.SubscriptionService;
import com.bbook.entity.Member;
import com.bbook.entity.Subscription.SubscriptionType;
import com.bbook.repository.SubscriptionRepository;
import com.bbook.config.MemberDetails;
import com.bbook.service.FirebaseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.bbook.entity.Subscription;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final FirebaseNotificationService firebaseNotificationService;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/subscribe")
    public String subscribePage() {
        return "subscription/subscribe";
    }

    @PostMapping("/complete")
    @ResponseBody
    public ResponseEntity<?> completeSubscription(@RequestBody Map<String, String> request,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        try {
            Member member = memberDetails.getMember();

            // 이미 활성화된 구독이 있는지 확인
            Optional<Subscription> existingSubscription = subscriptionRepository.findByMemberId(member.getId());
            if (existingSubscription.isPresent() && existingSubscription.get().isActive()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "이미 활성화된 구독이 존재합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String merchantUid = request.get("merchantUid");
            String impUid = request.get("impUid");
            String customerUid = request.get("customerUid");
            String subscriptionType = request.get("subscriptionType");

            SubscriptionType type = "MONTHLY".equals(subscriptionType) ? SubscriptionType.MONTHLY
                    : SubscriptionType.YEARLY;

            subscriptionService.createSubscription(member, type, merchantUid, impUid, customerUid);

            // 구독 성공 시 관리자에게 알림 전송
            firebaseNotificationService.sendSubscriptionNotification(
                    member.getNickname(),
                    type.toString());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "구독 완료 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkSubscription(
            @AuthenticationPrincipal MemberDetails memberDetails) {
        Map<String, Boolean> response = new HashMap<>();

        try {
            Member member = memberDetails.getMember();
            boolean isSubscriber = subscriptionRepository.findByMemberId(member.getId())
                    .map(subscription -> subscription.isActive())
                    .orElse(false);

            response.put("isSubscriber", isSubscriber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("isSubscriber", false);
            return ResponseEntity.ok(response);
        }
    }
}