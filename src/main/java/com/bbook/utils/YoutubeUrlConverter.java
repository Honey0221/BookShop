package com.bbook.utils;

import org.springframework.stereotype.Component;

@Component
public class YoutubeUrlConverter {
	public String convertToEmbedUrl(String youtubeUrl) {
		if (youtubeUrl == null || youtubeUrl.isEmpty()) {
			return null;
		}

		try {
			String videoId;
			System.out.println("URL 변환 시작" + youtubeUrl);
			if (youtubeUrl.contains("youtube.com/watch?v=")) {
				videoId = youtubeUrl.split("v=")[1];
			} else if (youtubeUrl.contains("youtu.be/")) {
				videoId = youtubeUrl.split("youtu.be/")[1];
			} else if (youtubeUrl.contains("youtube.com/embed/")) {
				return youtubeUrl; // 이미 임베드 URL인 경우
			} else {
				return null; // 유효하지 않은 URL
			}

			// 추가 파라미터 제거
			int ampersandPosition = videoId.indexOf('&');
			if (ampersandPosition != -1) {
				videoId = videoId.substring(0, ampersandPosition);
			}

			// 임베드 URL 반환
			return "https://www.youtube.com/embed/" + videoId;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
}
