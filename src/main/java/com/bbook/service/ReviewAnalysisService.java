package com.bbook.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.entity.Reviews;
import com.bbook.repository.ReviewRepository;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {
	@Value("${openai.api.key}")
	private String apiKey;

	private OpenAiService service;
	private final ReviewRepository reviewRepository;

	@PostConstruct
	public void init() {
		this.service = new OpenAiService(apiKey);
	}

	// 특정 책의 리뷰만 분석
	@Transactional
	public void analyzeBookReviews(Long bookId) {
		List<Reviews> reviews = reviewRepository.findByBookId(bookId);

		for (Reviews review : reviews) {
			AnalysisResult result = analyzeReview(review.getContent());

			// 악플인 경우 차단
			if (result.isHateSpeech()) {
				review.setBlocked(true);
				reviewRepository.save(review);
			}

			// 불쾌한 내용이 포함된 리뷰는 목록에 표시
			if (result.isUncomfortable()) {
				review.setFlagged(true);
				reviewRepository.save(review);
			}
		}
	}

	// 리뷰 분석 결과를 담는 클래스
	public record AnalysisResult(boolean isHateSpeech, boolean isUncomfortable) {}

	// 리뷰 분석 로직
	public AnalysisResult analyzeReview(String review) {
		try {
			CompletionRequest request = CompletionRequest.builder()
				.model("gpt-3.5-turbo-instruct")
				.prompt("리뷰 분석: " + review + "\n" +
					"아래 형식으로 정확히 답변해주세요:\n" +
					"악의적: [true/false] (욕설/비방/인신공격 포함 시 true)\n" +
					"부정적: [true/false] (불만/실망/아쉬움 표현 시 true)")
				.temperature(0.3)
				.maxTokens(50)
				.build();

			CompletionResult result = service.createCompletion(request);
			System.out.println(result);
			String analysis = result.getChoices().getFirst().getText().trim();
			
			System.out.println("=== GPT 응답 분석 시작 ===");
			System.out.println("전체 응답: " + analysis);

			String[] lines = analysis.split("\n");
			System.out.println("응답 줄 수: " + lines.length);
			
			for (int i = 0; i < lines.length; i++) {
				System.out.println("Line " + i + ": " + lines[i]);
			}

			// 악의적 표현 확인
			boolean isHateSpeech = false;
			if (lines[0].contains("악의적:")) {
				System.out.println("악의적 표현 라인: " + lines[0]);
				// true가 포함되어 있는지 직접 확인
				isHateSpeech = lines[0].toLowerCase().contains("true");
				System.out.println("악의적 표현 판단: " + isHateSpeech);
			}
			
			// 부정적 표현 확인
			boolean isUncomfortable = false;
			if (lines.length > 1 && lines[1].contains("부정적:")) {
				System.out.println("부정적 표현 라인: " + lines[1]);
				// true가 포함되어 있는지 직접 확인
				isUncomfortable = lines[1].toLowerCase().contains("true");
				System.out.println("부정적 표현 판단: " + isUncomfortable);
			}

			System.out.println("최종 분석 결과 - 악의적: " + isHateSpeech +
					", 부정적: " + isUncomfortable);
			System.out.println("=== GPT 응답 분석 완료 ===");

			return new AnalysisResult(isHateSpeech, isUncomfortable);
		} catch (Exception e) {
			System.out.println("=== 에러 발생 ===");
			System.out.println("에러 메시지: " + e.getMessage());
			e.printStackTrace();
			return new AnalysisResult(false, false);
		}
	}
}
