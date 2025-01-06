package com.bbook.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.constant.ActivityType;
import com.bbook.dto.recomendation.BookRecommendation;
import com.bbook.dto.recomendation.CategoryRecommendation;
import com.bbook.dto.recomendation.PersonalizedRecommendations;
import com.bbook.dto.recomendation.TimeBasedRecommendation;
import com.bbook.entity.Book;
import com.bbook.entity.MemberActivity;
import com.bbook.repository.BookRepository;
import com.bbook.repository.MemberActivityRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberActivityService {
	private final MemberActivityRepository activityRepository;
	private final BookRepository bookRepository;
	private static final int DEFAULT_RECOMMENDATION_SIZE = 10;
	private static final int DEFAULT_DAYS_FOR_RECENT = 30;

	// 활동 기록 저장
	public void saveActivity(String memberEmail, Long bookId, ActivityType activityType) {
		try {
			// 책 정보 조회
			Optional<Book> book = bookRepository.findById(bookId);

			MemberActivity activity = MemberActivity.builder()
					.memberEmail(memberEmail)
					.bookId(bookId)
					.activityType(activityType)
					.mainCategory(
							book.map(Book::getMainCategory).orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId)))
					.midCategory(
							book.map(Book::getMidCategory).orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId)))
					.detailCategory(book.map(Book::getDetailCategory)
							.orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId)))
					.build();

			activityRepository.save(activity);
			log.info("Activity saved - memberEmail: {}, bookId: {}, type: {}",
					memberEmail, bookId, activityType);

		} catch (Exception e) {
			log.error("Failed to save activity", e);
			throw new RuntimeException("활동 기록 저장 실패", e);
		}
	}

	// 활동 취소 메서드
	@Transactional
	public void cancelActivity(String memberEmail, Long bookId, ActivityType activityType) {
		// 활성화된 특정 활동 찾기
		MemberActivity activity = activityRepository.findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalse(
				memberEmail, bookId, activityType);

		if (!activity.isCancellable()) {
			throw new IllegalStateException("취소할 수 없는 활동입니다.");
		}

		activity.cancel();
		log.info("Activity canceled - email: {}, bookId: {}, type: {}",
				memberEmail, bookId, activityType);
	}

	// === 추천 시스템 관련 메서드 ===

	// 1. 사용자 맞춤 카테고리 추천
	@Transactional(readOnly = true)
	public List<CategoryRecommendation> recommendCategories(String email) {
		LocalDateTime since = LocalDateTime.now().minusDays(DEFAULT_DAYS_FOR_RECENT);

		List<Object[]> categoryPreferences = activityRepository
				.findCategoryPreferences(email, since);

		return categoryPreferences.stream()
				.map(result -> CategoryRecommendation.builder()
						.category((String) result[0])
						.score(calculatePreferenceScore((Long) result[1]))
						.build())
				.collect(Collectors.toList());
	}

	// 2. 시간대별 추천
	@Transactional(readOnly = true)
	public List<TimeBasedRecommendation> getTimeBasedRecommendations(String email) {
		List<Object[]> hourlyPatterns = activityRepository
				.findHourlyActivityPattern(email);

		return hourlyPatterns.stream()
				.map(result -> TimeBasedRecommendation.builder()
						.hour((Integer) result[0])
						.activityCount((Long) result[1])
						.build())
				.collect(Collectors.toList());
	}

	// 3. 카테고리별 인기 도서 추천
	@Transactional(readOnly = true)
	public List<BookRecommendation> recommendPopularBooks(String category) {
		LocalDateTime since = LocalDateTime.now().minusDays(DEFAULT_DAYS_FOR_RECENT);
		PageRequest pageRequest = PageRequest.of(0, DEFAULT_RECOMMENDATION_SIZE);

		List<Object[]> popularBooks = activityRepository
				.findPopularBooksByCategory(category, since, pageRequest);

		return popularBooks.stream()
				.map(result -> BookRecommendation.builder()
						.bookId((Long) result[0])
						.popularityScore(calculatePopularityScore((Long) result[1]))
						.build())
				.collect(Collectors.toList());
	}

	// 4. 개인화된 종합 추천
	@Transactional(readOnly = true)
	public PersonalizedRecommendations getPersonalizedRecommendations(String email) {
		LocalDateTime since = LocalDateTime.now().minusDays(DEFAULT_DAYS_FOR_RECENT);

		return PersonalizedRecommendations.builder()
				.categoryRecommendations(recommendCategories(email))
				.timeBasedRecommendations(getTimeBasedRecommendations(email))
				.recentCategories(activityRepository.findRecentCategories(
						email, since, PageRequest.of(0, 5)))
				.build();
	}

	// === 헬퍼 메서드 ===
	private double calculatePreferenceScore(Long count) {
		return Math.log10(count + 1) * 10;
	}

	private double calculatePopularityScore(Long count) {
		return Math.log10(count + 1) * 10;
	}
}
