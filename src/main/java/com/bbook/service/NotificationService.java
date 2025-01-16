package com.bbook.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    public void sendToAdmin(String message) {
        
        System.out.println("Admin Notification: " + message);
    }
}