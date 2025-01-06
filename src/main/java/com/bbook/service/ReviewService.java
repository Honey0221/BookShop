package com.bbook.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewUpdateDto;
import com.bbook.entity.Reviews;
import com.bbook.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
	private final ReviewRepository reviewRepository;

	public void saveReview(ReviewDto reviewDto) {
		Reviews review = Reviews.builder()
				.memberId(reviewDto.getMemberId())
				.bookId(reviewDto.getBookId())
				.rating(reviewDto.getRating())
				.content(reviewDto.getContent())
				.createdAt(LocalDateTime.now())
				.build();

		reviewRepository.save(review);
	}

	public Page<ReviewDto> getBookReviews(
			Long bookId, Long currentMemberId, Pageable pageable) {
		Page<ReviewDto> reviews = reviewRepository.findByBookId(bookId, pageable);

		return reviews.map(review -> ReviewDto.builder()
				.id(review.getId())
				.bookId(review.getBookId())
				.memberId(review.getMemberId())
				.memberName(review.getMemberName())
				.rating(review.getRating())
				.content(review.getContent())
				.createdAt(review.getCreatedAt())
				.isOwner(currentMemberId != null && currentMemberId.equals(review.getMemberId()))
				.build());
	}

	public Double getAverageRatingByBookId(Long bookId) {
		return reviewRepository.getAverageRatingByBookId(bookId);
	}

	public void deleteReview(Long reviewId) {
		reviewRepository.deleteById(reviewId);
	}

	public void updateReview(Long reviewId, Long memberId,
			ReviewUpdateDto updateDto) {
		Reviews reviews = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

		if (!reviews.getMemberId().equals(memberId)) {
			throw new RuntimeException("리뷰 수정 권한이 없습니다.");
		}

		reviews.setRating(updateDto.getRating());
		reviews.setContent(updateDto.getContent());

		reviewRepository.save(reviews);
	}

	public long getReviewCount(Long bookId) {
		return reviewRepository.countByBookId(bookId);
	}
}
