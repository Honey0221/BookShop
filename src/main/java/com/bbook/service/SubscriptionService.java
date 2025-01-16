package com.bbook.service;

import com.bbook.entity.Member;
import com.bbook.entity.Subscription;
import com.bbook.entity.Subscription.SubscriptionType;
import com.bbook.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @Value("${imp.api.key}")
    private String IMP_KEY;

    @Value("${imp.api.secret}")
    private String IMP_SECRET;

    @Transactional
    public void createSubscription(Member member, SubscriptionType type, String merchantUid, String impUid,
            String customerUid) {
        // 이미 활성화된 구독이 있는지 확인
        Optional<Subscription> existingSubscription = subscriptionRepository.findByMemberId(member.getId());
        if (existingSubscription.isPresent() && existingSubscription.get().isActive()) {
            throw new RuntimeException("이미 활성화된 구독이 존재합니다.");
        }

        // 빌링키 발급 확인
        try {
            // 아임포트 토큰 발급
            String accessToken = getIamportToken();

            // 빌링키 조회
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> billingResponse = restTemplate.exchange(
                    "https://api.iamport.kr/subscribe/customers/" + customerUid,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            // log.info("빌링키 조회 응답: {}", billingResponse.getBody());

            if (billingResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("빌링키 조회 실패");
            }

            // 구독 생성
            Subscription subscription = Subscription.createSubscription(member, type);
            subscription.setMerchantUid(merchantUid);
            subscription.setImpUid(impUid);
            subscription.setCustomerUid(customerUid);

            subscriptionRepository.save(subscription);

            String message = String.format("[새로운 구독] %s님이 %s 구독을 시작했습니다.",
                    member.getNickname(),
                    type == SubscriptionType.MONTHLY ? "월간" : "연간");
            notificationService.sendToAdmin(message);

        } catch (Exception e) {
            log.error("구독 생성 중 오류 발생", e);
            throw new RuntimeException("구독 생성 실패: " + e.getMessage());
        }
    }

    private String getIamportToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("imp_key", IMP_KEY);
        formData.add("imp_secret", IMP_SECRET);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> tokenEntity = new HttpEntity<>(formData, tokenHeaders);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                "https://api.iamport.kr/users/getToken",
                HttpMethod.POST,
                tokenEntity,
                Map.class);

        if (tokenResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("토큰 발급 실패");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> responseData = (Map<String, Object>) tokenResponse.getBody().get("response");
        return (String) responseData.get("access_token");
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processRecurringPayments() {
        // log.info("정기 결제 프로세스 시작 - 현재 시간: {}", LocalDateTime.now());

        List<Subscription> activeSubscriptions = subscriptionRepository.findByIsActiveTrue();
        // log.info("활성 구독 수: {}", activeSubscriptions.size());

        for (Subscription subscription : activeSubscriptions) {
            try {
                // log.info("구독 정보 확인 - ID: {}, 회원: {}, 시작일: {}, 다음 결제일: {}, CustomerUID: {}",
                // subscription.getId(),
                // subscription.getMember().getNickname(),
                // subscription.getStartDate(),
                // subscription.getNextPaymentDate(),
                // subscription.getCustomerUid());

                // nextPaymentDate가 null인 경우 1분 후로 설정
                // if (subscription.getNextPaymentDate() == null) {
                // subscription.setNextPaymentDate(LocalDateTime.now().plusMinutes(1));
                // subscriptionRepository.save(subscription);
                // log.info("다음 결제일 설정됨: {}", subscription.getNextPaymentDate());
                // continue; // 이번 주기에서는 결제 시도하지 않음
                // }

                // 결제 주기 확인
                LocalDateTime nextPaymentDate = subscription.getNextPaymentDate();
                LocalDateTime now = LocalDateTime.now();

                // log.info("결제 시간 확인 - 현재 시간: {}, 다음 결제일: {}, 결제 필요: {}",
                // now, nextPaymentDate, now.isAfter(nextPaymentDate));

                // 현재 시간이 다음 결제일보다 이후인 경우에만 결제 진행
                if (now.isAfter(nextPaymentDate)) {
                    // log.info("구독 결제 시작 - 구독 ID: {}, 회원: {}",
                    // subscription.getId(),
                    // subscription.getMember().getNickname());

                    processPayment(subscription);

                    // 결제 성공 시 다음 결제일 업데이트
                    LocalDateTime newNextPaymentDate = subscription.getType() == SubscriptionType.MONTHLY
                            ? now.plusMinutes(1)
                            : now.plusMinutes(1);

                    subscription.setNextPaymentDate(newNextPaymentDate);
                    subscriptionRepository.save(subscription);

                    // log.info("다음 결제일 업데이트 완료 - 새로운 결제일: {}", newNextPaymentDate);
                } else {
                    log.info("아직 결제 시기가 아님 - 구독 ID: {}, 다음 결제일: {}",
                            subscription.getId(), nextPaymentDate);
                }
            } catch (Exception e) {
                notificationService.sendToAdmin(String.format("[정기결제 실패] %s님의 구독 갱신에 실패했습니다: %s",
                        subscription.getMember().getNickname(), e.getMessage()));
            }
        }
    }

    // 실제 결제 처리 메서드 분리
    private void processPayment(Subscription subscription) {
        String customerUid = subscription.getCustomerUid();
        // log.info("정기결제 시작 - Customer UID: {}", customerUid);

        // 구독 타입에 따른 결제 금액 설정
        int amount = subscription.getPrice();
        // log.info("구독 타입: {}, 결제 금액: {}", subscription.getType(), amount);

        try {
            // 아임포트 토큰 발급 요청
            // log.info("아임포트 토큰 발급 시도");

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("imp_key", IMP_KEY);
            formData.add("imp_secret", IMP_SECRET);

            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> tokenEntity = new HttpEntity<>(formData, tokenHeaders);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    "https://api.iamport.kr/users/getToken",
                    HttpMethod.POST,
                    tokenEntity,
                    Map.class);

            // log.info("토큰 응답: {}", tokenResponse.getBody());

            if (tokenResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("토큰 발급 실패: " + tokenResponse.getBody());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = (Map<String, Object>) tokenResponse.getBody().get("response");
            String accessToken = (String) responseData.get("access_token");
            // log.info("액세스 토큰 발급 성공: {}", accessToken);

            // 결제 요청 헤더에 토큰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 결제 요청 데이터
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("customer_uid", customerUid);
            paymentData.put("merchant_uid", "subscription_" + System.currentTimeMillis());
            paymentData.put("amount", amount);
            paymentData.put("name", "BBOOK " +
                    (subscription.getType() == SubscriptionType.MONTHLY ? "월간" : "연간") + " 구독");
            paymentData.put("pg", "tosspayments");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentData, headers);
            // log.info("정기결제 요청 데이터: {}", paymentData);

            // 실제 결제 요청
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> result = restTemplate.exchange(
                    "https://api.iamport.kr/subscribe/payments/again",
                    HttpMethod.POST,
                    entity,
                    Map.class);

            // log.info("정기결제 응답: {}", result.getBody());

            if (result.getStatusCode() == HttpStatus.OK) {
                // log.info("정기결제 성공 - 구독자: {}", subscription.getMember().getNickname());
                notificationService.sendToAdmin(String.format("[정기결제 성공] %s님의 구독이 갱신되었습니다.",
                        subscription.getMember().getNickname()));
            } else {
                throw new RuntimeException("결제 실패: " + result.getBody());
            }
        } catch (Exception e) {
            log.error("정기결제 처리 중 오류 발생", e);
            throw new RuntimeException("정기결제 처리 실패: " + e.getMessage());
        }
    }
}