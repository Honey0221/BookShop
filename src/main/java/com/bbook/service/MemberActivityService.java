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
			// 책 정보 조회
			Optional<Book> book = bookRepository.findById(bookId);

			MemberActivity activity = MemberActivity.builder()
					.memberEmail(memberEmail)
					.bookId(bookId)
					.activityType(activityType)
					.mainCategory(book.map(Book::getMainCategory).orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId)))
					.midCategory(book.map(Book::getMidCategory).orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId)))
					.detailCategory(book.map(Book::getDetailCategory).orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId)))
					.build();

			activityRepository.save(activity);
			log.info("Activity saved - memberEmail: {}, bookId: {}, type: {}",
					memberEmail, bookId, activityType);

		} catch (Exception e) {
			log.error("Failed to save activity", e);
			throw new RuntimeException("활동 기록 저장 실패", e);
		}
	}
}
