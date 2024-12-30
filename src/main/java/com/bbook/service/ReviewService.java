package com.bbook.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.dto.ReviewDto;
import com.bbook.entity.Reviews;
import com.bbook.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;

	@Transactional
	public void saveReview(Long memberId, Long bookId, int rating, String content) {
		Reviews review = Reviews.builder()
				.member_id(memberId)
				.book_id(bookId)
				.rating(rating)
				.content(content)
				.build();

		reviewRepository.save(review);
	}

	@Transactional(readOnly = true)
	public List<ReviewDto> getBookReviews(Long bookId) {
		return reviewRepository.findByBookId(bookId);
	}
}
