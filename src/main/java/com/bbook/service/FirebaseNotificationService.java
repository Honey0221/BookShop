package com.bbook.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class FirebaseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseNotificationService.class);

    private List<String> adminTokens = new ArrayList<>();

    public void addAdminToken(String token) {
        if (!adminTokens.contains(token)) {
            adminTokens.add(token);
        }
    }

    public void sendSubscriptionNotification(String memberName, String subscriptionType) {
        try {
            log.info("알림 전송 시작 - 구독자: {}, 구독타입: {}", memberName, subscriptionType);
            log.info("현재 등록된 관리자 토큰 수: {}", adminTokens.size());

            // 모든 관리자 토큰으로 메시지 전송
            for (String token : adminTokens) {
                Message tokenMessage = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle("새로운 구독 알림")
                                .setBody(memberName + "님이 " + subscriptionType + " 구독을 시작했습니다.")
                                .build())
                        .putData("type", "subscription")
                        .putData("memberName", memberName)
                        .putData("subscriptionType", subscriptionType)
                        .setToken(token)
                        .build();

                String response = FirebaseMessaging.getInstance().send(tokenMessage);
                log.info("알림 전송 완료 - 토큰: {}, 응답: {}", token.substring(0, 20) + "...", response);
            }
        } catch (Exception e) {
            log.error("알림 전송 실패", e);
        }
    }
}