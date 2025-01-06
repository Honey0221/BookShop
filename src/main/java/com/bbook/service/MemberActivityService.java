package com.bbook.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.constant.ActivityType;
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

	// 활동 기록 저장
	public void saveActivity(String memberEmail, Long bookId, ActivityType activityType) {
		try {
			// 이미 활성화된 동일한 활동이 있는지 확인
			Optional<MemberActivity> existingActivity = activityRepository
					.findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalseOrderByActivityTimeDesc(
							memberEmail, bookId, activityType);

			// 이미 활성화된 동일한 활동이 있다면 저장하지 않음
			if (existingActivity.isPresent()) {
				log.info("Already exists active activity - memberEmail: {}, bookId: {}, type: {}",
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

}
