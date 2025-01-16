package com.bbook.service;

import com.bbook.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final WebClient slackWebClient;

    @Async
    public void sendStockAlert(Book book) {
        String message = createStockAlertMessage(book);

        Map<String, Object> body = Map.of(
                "text", message);

        slackWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("재고 알림 전송 성공: {}", book.getTitle()))
                .doOnError(error -> log.error("재고 알림 전송 실패: {}", error.getMessage()))
                .subscribe();
    }

    private String createStockAlertMessage(Book book) {
        return String.format("""
                :rotating_light: *재고 소진 알림* :rotating_light:

                :book: *도서 정보*
                • 도서명: `%s`
                • 작성자: %s
                • 카테고리: %s
                • 최종 재고 소진 시간: %s

                :warning: *빠른 재고 확보가 필요합니다!*
                """,
                book.getTitle(),
                book.getAuthor(),
                book.getDetailCategory(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}