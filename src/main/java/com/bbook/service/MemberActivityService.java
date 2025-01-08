package com.bbook.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.constant.ActivityType;
import com.bbook.dto.BookRecommendationDto;
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
	private static final int RECOMMENDATION_SIZE = 10;
	private static final int MIN_COMMON_BOOKS = 1;
	private static final int MIN_SIMILAR_USERS = 1;
	private static final int RECENT_VIEW_SIZE = 10;

	// 활동 기록 저장
	public void saveActivity(String memberEmail, Long bookId, ActivityType activityType) {
		try {
			// 이미 활성화된 동일한 활동이 있는지 확인
			Optional<MemberActivity> existingActivity = activityRepository
					.findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalseOrderByActivityTimeDesc(
							memberEmail, bookId, activityType);

			// 이미 활성화된 동일한 활동이 있다면 저장하지 않음
			if (existingActivity.isPresent()) {
				MemberActivity activity = existingActivity.get(); // 기존 엔티티 가져오기
				activity.updateActivityTime(LocalDateTime.now()); // 시간만 업데이트
				activityRepository.save(activity); // 같은 엔티티를 다시 저장
				log.info("Activity time updated - memberEmail: {}, bookId: {}, type: {}",
						memberEmail, bookId, activityType);
				return;
			}
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
		MemberActivity activity = activityRepository
				.findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalse(
						memberEmail, bookId, activityType);

		if (!activity.isCancellable()) {
			throw new IllegalStateException("취소할 수 없는 활동입니다.");
		}

		activity.cancel();
		log.info("Activity canceled - email: {}, bookId: {}, type: {}",
				memberEmail, bookId, activityType);
	}

	// 최근 본 도서 조회
	public List<Book> getRecentViewedBooks(String email) {
		List<MemberActivity> recentActivities = activityRepository.findRecentActivities(
				email,
				ActivityType.VIEW,
				PageRequest.of(0, RECENT_VIEW_SIZE));

		return recentActivities.stream()
				.map(activity -> bookRepository.findById(activity.getBookId())
						.orElseThrow(() -> new EntityNotFoundException("Book not found: " + activity.getBookId())))
				.collect(Collectors.toList());
	}

	// 1. 컨텐츠 기반 추천
	@Cacheable(value = "contentBasedRecommendations", key = "#email")
	public List<BookRecommendationDto> getContentBasedRecommendations(String email) {
		log.info("Calculating content-based recommendations for user: {}", email);
		LocalDateTime since = LocalDateTime.now().minusDays(30);

		List<Object[]> preferences = activityRepository.findUserCategoryPreferences(email, since);
		Map<Long, Double> scores = new HashMap<>();

		// 카테고리 선호도 기반 점수 계산
		for (Object[] pref : preferences) {
			String mainCategory = (String) pref[0];
			String midCategory = (String) pref[1];
			String detailCategory = (String) pref[2];
			Long count = (Long) pref[3];
			double weight = Math.log10(count + 1);

			// 카테고리 기반 도서 점수 계산 로직
			calculateCategoryBasedScores(mainCategory, midCategory, detailCategory, weight, scores);
		}
		// 점수를 기준으로 상위 N개 책 추천
		return scores.entrySet().stream()
				.sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
				.limit(RECOMMENDATION_SIZE)
				.map(entry -> {
					Book book = bookRepository.findById(entry.getKey())
							.orElseThrow(() -> new EntityNotFoundException("Book not found: " + entry.getKey()));
					return BookRecommendationDto.from(book, entry.getValue());
				})
				.collect(Collectors.toList());
	}

	// 2. 협업 필터링 기반 추천
	@Cacheable(value = "collaborativeRecommendations", key = "#email")
	public List<BookRecommendationDto> getCollaborativeRecommendations(String email) {
		log.info("Calculating collaborative recommendations for user: {}", email);
		LocalDateTime since = LocalDateTime.now().minusDays(30);

		// 유사 사용자 찾기
		List<Object[]> similarUsers = activityRepository.findSimilarUsers(
				email, since, MIN_COMMON_BOOKS, PageRequest.of(0, 10));

		if (similarUsers.isEmpty()) {
			return Collections.emptyList();
		}

		// 유사 사용자 기반 추천
		List<String> similarUserEmails = similarUsers.stream()
				.map(u -> (String) u[0])
				.collect(Collectors.toList());

		List<Object[]> recommendations = activityRepository.findRecommendedBooksBySimilarUsers(
				email, similarUserEmails, MIN_SIMILAR_USERS, PageRequest.of(0, RECOMMENDATION_SIZE));

		return convertCollaborativeRecommendations(recommendations);
	}

	// 3. 하이브리드 추천
	@Cacheable(value = "hybridRecommendations", key = "#email")
	public List<BookRecommendationDto> getHybridRecommendations(String email) {
		List<BookRecommendationDto> contentBased = getContentBasedRecommendations(email);
		List<BookRecommendationDto> collaborative = getCollaborativeRecommendations(email);

		Map<Long, Double> hybridScores = new HashMap<>();

		// 컨텐츠 기반 (40% 가중치)
		contentBased.forEach(rec -> hybridScores.merge(rec.getBookId(), rec.getScore() * 0.4, Double::sum));

		// 협업 필터링 (60% 가중치)
		collaborative.forEach(rec -> hybridScores.merge(rec.getBookId(), rec.getScore() * 0.6, Double::sum));

		return convertToRecommendations(hybridScores);
	}

	// 헬퍼 메서드들
	private List<BookRecommendationDto> convertToRecommendations(Map<Long, Double> scores) {
		return scores.entrySet().stream()
				.sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
				.limit(RECOMMENDATION_SIZE)
				.map(entry -> {
					Book book = bookRepository.findById(entry.getKey())
							.orElseThrow(() -> new EntityNotFoundException("Book not found: " + entry.getKey()));
					return BookRecommendationDto.from(book, entry.getValue());
				})
				.collect(Collectors.toList());
	}

	// 카테고리 기반 점수 계산
	private void calculateCategoryBasedScores(String mainCategory,
			String midCategory,
			String detailCategory,
			double weight,
			Map<Long, Double> scores) {
		// 카테고리와 일치하는 책들 찾기
		List<Book> similarBooks = bookRepository.findByMainCategoryOrMidCategoryOrDetailCategory(
				mainCategory, midCategory, detailCategory);

		// 각 책에 대해 카테고리 일치도에 따른 점수 계산
		for (Book book : similarBooks) {
			double score = 0.0;

			// 대분류 일치: 3점
			if (book.getMainCategory().equals(mainCategory)) {
				score += 3.0;
			}
			// 중분류 일치: 2점
			if (book.getMidCategory().equals(midCategory)) {
				score += 2.0;
			}
			// 소분류 일치: 1점
			if (book.getDetailCategory().equals(detailCategory)) {
				score += 1.0;
			}

			// 최종 점수 = 카테고리 점수 * 사용자 선호도 가중치
			double finalScore = score * weight;

			// 기존 점수와 병합
			scores.merge(book.getId(), finalScore, Double::sum);
		}
	}

	// 협업 필터링 결과 변환
	private List<BookRecommendationDto> convertCollaborativeRecommendations(List<Object[]> recommendations) {
		return recommendations.stream()
				.map(rec -> {
					Long bookId = (Long) rec[0];
					Long userCount = (Long) rec[1];
					Double score = ((Number) rec[2]).doubleValue();

					// 정규화된 점수 계산 (사용자 수와 활동 점수 반영)
					// 사용자수 100000명 정도 수식
					// double normalizedScore = score * Math.log10(userCount + 1);
					// 사용자수 1~2명 정도 수식
					double normalizedScore = (score * 5) * (Math.log10(userCount + 5));

					// 책 정보 조회
					Book book = bookRepository.findById(bookId)
							.orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));

					// DTO 변환
					return BookRecommendationDto.from(book, normalizedScore);
				})
				.collect(Collectors.toList());
	}
}
