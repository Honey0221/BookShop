package com.bbook.service.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bbook.dto.BookFormDto;
import com.bbook.dto.ReviewAlertDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TelegramAlertService {
	@Value("${telegram.bot.token}")
	private String botToken;

	@Value("${telegram.chat.id}")
	private String chatId;

	// 알림 테스트
	public void sendTestMessage() {
		String message = """
				🔔 [BBook 알림 테스트]
				\s
				알림 서비스가 정상적으로 설정되었습니다.
				이제부터 알림을 받을 수 있습니다.
				""";

		sendMessage(message);
	}

	// 신규 상품 등록 알림 발송
	public void sendNewBookAlert(BookFormDto bookFormDto) {
		// 카테고리 경로 동적 생성
		StringBuilder categoryPath = new StringBuilder();
		categoryPath.append(bookFormDto.getMainCategory());
		categoryPath.append(" > ").append(bookFormDto.getMidCategory());
		categoryPath.append(" > ").append(bookFormDto.getSubCategory());

		if (bookFormDto.getDetailCategory() == null) {
			categoryPath.append(" > ").append(bookFormDto.getDetailCategory());
		}

		String message = String.format("""
				📚 [BBook 신규 상품 알림]
						\s
				✨ 새로운 도서가 등록되었습니다!
						\s
				📖 도서 정보:
				• 제목: %s
				• 저자: %s
				• 출판사: %s
				• 가격: %,d원
				• 카테고리: %s
						\s
				🔍 관리자 페이지에서 확인해주세요.
				""",
				bookFormDto.getTitle(),
				bookFormDto.getAuthor(),
				bookFormDto.getPublisher(),
				bookFormDto.getPrice(),
				categoryPath);

		sendMessage(message);
	}

	// 신고 알림 발송
	public void sendReportAlert(ReviewAlertDto reviewAlertDto) {
		String message = String.format("""
				🚨 [BBook 신고 알림]
					\s
				⚠️ 신고 누적 리뷰 발생
					\s
				📝 리뷰 정보:
				• ID: %d
				• 신고 횟수: %d회
				• 작성자: %s
				• 리뷰 내용: %s
					\s
				⚡ 즉시 처리가 필요합니다!
				""",
				reviewAlertDto.getReviewId(),
				reviewAlertDto.getReportCount(),
				reviewAlertDto.getMemberNickname(),
				reviewAlertDto.getContent()
						.substring(0, Math.min(reviewAlertDto.getContent().length(), 50)) + "...");

		sendMessage(message);
	}

	// 메시지 전송 메소드
	private void sendMessage(String message) {
		// 텔레그램 봇 API URL 생성
		String url = String.format(
				"https://api.telegram.org/bot%s/sendMessage", botToken);

		try {
			// HTTP 요청을 위한 RestTemplate 생성
			RestTemplate restTemplate = new RestTemplate();

			// HTTP 헤더 설정
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// 요청할 Body 설정
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("chat_id", chatId);
			requestBody.put("text", message);
			requestBody.put("parse_mode", "HTML");

			// HTTP 요청 엔티티 생성
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

			// Post 요청 전송 및 응답 수신
			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

			// 응답 결과
			if (response.getStatusCode() == HttpStatus.OK) {
				System.out.println("알림 발송 성공");
			} else {
				System.out.println("알림 발송 실패 " + response.getBody());
			}
		} catch (Exception e) {
			System.out.println("오류 발생 : " + e.getMessage());
		}
	}
}
