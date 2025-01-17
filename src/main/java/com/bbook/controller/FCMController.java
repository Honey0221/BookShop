package com.bbook.controller;

import com.bbook.service.FirebaseNotificationService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FCMController {

    private final FirebaseNotificationService firebaseNotificationService;

    @PostMapping("/token")
    public void registerToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        log.info("새로운 FCM 토큰 등록 요청: {}", token);
        try {
            firebaseNotificationService.addAdminToken(token);
            log.info("FCM 토큰 등록 완료");
        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패", e);
            throw e;
        }
    }
}