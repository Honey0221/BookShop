package com.bbook.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbook.constant.ReportStatus;
import com.bbook.constant.ReportType;
import com.bbook.constant.TagType;
import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewStatsDto;
import com.bbook.dto.ReviewUpdateDto;
import com.bbook.entity.Member;
import com.bbook.entity.ReviewLike;
import com.bbook.entity.ReviewReport;
import com.bbook.entity.Reviews;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.ReviewLikeRepository;
import com.bbook.repository.ReviewReportRepository;
import com.bbook.repository.ReviewRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final ReviewLikeRepository likeRepository;
	private final ReviewReportRepository reportRepository;
	private final MemberRepository memberRepository;
	private final FileService fileService;

	@Value("${reviewImgLocation}")
	private String reviewImgLocation;

	public void createReview(ReviewDto reviewDto) {
		Reviews review = Reviews.builder()
				.memberId(reviewDto.getMemberId())
				.bookId(reviewDto.getBookId())
				.rating(reviewDto.getRating())
				.content(reviewDto.getContent())
				.createdAt(LocalDateTime.now())
				.tagType(reviewDto.getTagType())
				.build();

		if (reviewDto.getReviewImages() != null && !reviewDto.getReviewImages()
				.isEmpty()) {
			for (MultipartFile file : reviewDto.getReviewImages()) {
				try {
					String originalFilename = file.getOriginalFilename();
					String savedFilename = fileService.uploadFile(reviewImgLocation,
							originalFilename, file.getBytes());
					review.addImage(savedFilename);
				} catch (Exception e) {
					throw new RuntimeException("이미지 업로드에 실패하였습니다.", e);
				}
			}
		}

		reviewRepository.save(review);
	}

	public Page<ReviewDto> getBookReviews(
			Long bookId, Long currentMemberId, Pageable pageable, String sort) {
		Page<Reviews> reviewsPage;

		if ("likes".equals(sort)) {
			reviewsPage = reviewRepository.findByBookIdOrderByLikeCountDesc(bookId, pageable);
		} else {
			reviewsPage = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);
		}

		return reviewsPage.map(review -> {
			String memberName = memberRepository.findById(review.getMemberId())
					.map(Member::getNickname)
					.orElse("Unknown");

			int likeCount = likeRepository.countByReviewId(review.getId());

			boolean isLiked = false;
			if (currentMemberId != null) {
				isLiked = likeRepository
						.existsByReviewIdAndMemberId(review.getId(), currentMemberId);
			}

			return ReviewDto.builder()
					.id(review.getId())
					.bookId(review.getBookId())
					.memberId(review.getMemberId())
					.memberName(memberName)
					.rating(review.getRating())
					.content(review.getContent())
					.images(review.getImages())
					.createdAt(review.getCreatedAt())
					.isOwner(currentMemberId != null && currentMemberId.equals(review.getMemberId()))
					.tagType(review.getTagType())
					.likeCount(likeCount)
					.isLiked(isLiked)
					.build();
		});
	}

	public Double getAverageRatingByBookId(Long bookId) {
		return reviewRepository.getAverageRatingByBookId(bookId);
	}

	public void deleteReview(Long reviewId) {
		Reviews review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

		if (!review.getImages().isEmpty()) {
			for (String imageUrl : review.getImages()) {
				try {
					fileService.deleteFile(reviewImgLocation, imageUrl);
				} catch (Exception e) {
					throw new RuntimeException("이미지 삭제에 실패하였습니다.", e);
				}
			}
		}

		reviewRepository.deleteById(reviewId);
	}

	public void updateReview(Long reviewId, Long memberId,
			ReviewUpdateDto updateDto) {
		Reviews review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

		if (!review.getMemberId().equals(memberId)) {
			throw new RuntimeException("리뷰 수정 권한이 없습니다.");
		}

		review.setRating(updateDto.getRating());
		review.setContent(updateDto.getContent());
		if (updateDto.getTagType() != null) {
			review.setTagType(updateDto.getTagType());
		}

		if (review.getImages() != null && !review.getImages().isEmpty()) {
			for (String imageUrl : review.getImages()) {
				try {
					fileService.deleteFile(reviewImgLocation, imageUrl);
				} catch (Exception e) {
					throw new RuntimeException("이미지 삭제에 실패하였습니다", e);
				}
			}
			review.getImages().clear();
		}

		if (updateDto.getReviewImages() != null && !updateDto.getReviewImages()
				.isEmpty()) {
			for (MultipartFile file : updateDto.getReviewImages()) {
				try {
					String originalFilename = file.getOriginalFilename();
					String savedFilename = fileService.uploadFile(reviewImgLocation,
							originalFilename, file.getBytes());
					review.addImage(savedFilename);
				} catch (Exception e) {
					throw new RuntimeException("이미지 업로드에 실패하였습니다.", e);
				}
			}
		}

		reviewRepository.save(review);
	}

	public long getReviewCount(Long bookId) {
		return reviewRepository.countByBookId(bookId);
	}

	public Map<String, Object> toggleLike(Long reviewId, Long memberId) {
		Reviews review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
		// 이미 좋아요 했는지 체크
		Optional<ReviewLike> existingLike = likeRepository.findByReviewIdAndMemberId(reviewId, memberId);

		boolean isLiked;
		if (existingLike.isPresent()) {
			// 좋아요 취소
			likeRepository.delete(existingLike.get());
			review.decreaseLikeCount();
			isLiked = false;
		} else {
			// 좋아요 추가
			ReviewLike reviewLike = ReviewLike.builder()
					.memberId(memberId)
					.reviewId(reviewId)
					.bookId(review.getBookId())
					.createdAt(LocalDateTime.now())
					.build();
			likeRepository.save(reviewLike);
			review.increaseLikeCount();
			isLiked = true;
		}

		int likeCount = likeRepository.countByReviewId(reviewId);

		return Map.of("isLiked", isLiked, "likeCount", likeCount);
	}

	public ReviewStatsDto getReviewStats(Long bookId) {
		List<Reviews> reviews = reviewRepository.findByBookId(bookId);
		ReviewStatsDto stats = new ReviewStatsDto();

		// 평점 통계 계산
		Map<Integer, Long> ratingCounts = reviews.stream()
				.collect(Collectors.groupingBy(Reviews::getRating, Collectors.counting()));

		int totalReviews = reviews.size();
		Map<Integer, Double> ratingStats = new HashMap<>();

		// 평점별 비율 계산
		for (int i = 1; i <= 5; i++) {
			long count = ratingCounts.getOrDefault(i, 0L);
			double percentage = totalReviews > 0 ? (count * 100.0) / totalReviews : 0;
			ratingStats.put(i, percentage);
		}

		// 평균 평점 계산
		double avgRating = reviews.stream()
				.mapToInt(Reviews::getRating)
				.average().orElse(0.0);

		// 태그 통계 계산
		Map<String, Double> tagStats = new HashMap<>();
		String mostCommonTag = "";

		// 유효한 태그가 있는 리뷰만 필터링
		List<Reviews> reviewsWithTags = reviews.stream()
				.filter(r -> r.getTagType() != null)
				.collect(Collectors.toList());

		if (!reviewsWithTags.isEmpty()) {
			// 태그별 카운트 계산
			Map<String, Long> tagCounts = reviewsWithTags.stream()
					.collect(Collectors.groupingBy(
							r -> r.getTagType().toString(),
							Collectors.counting()));

			// 총 태그 수 계산
			long totalTags = reviewsWithTags.size();

			// 태그별 비율 계산
			tagCounts.forEach((tag, count) -> {
				double percentage = (count * 100.0) / totalTags;
				tagStats.put(tag, percentage);
			});

			// 가장 많이 사용된 태그 찾기
			if (!tagCounts.isEmpty()) {
				mostCommonTag = Collections.max(tagCounts.entrySet(), Map.Entry.comparingByValue())
						.getKey();
			}
		}

		stats.setRatingStats(ratingStats);
		stats.setAvgRating(avgRating);
		stats.setTagStats(tagStats);
		stats.setMostCommonTag(mostCommonTag);

		return stats;
	}

	// 리뷰 신고하기
	public void reportReview(
			Long reviewId, Long memberId, ReportType reportType, String content) {
		// 이미 신고한 리뷰인지 확인
		if (reportRepository.existsByReviewIdAndMemberId(reviewId, memberId)) {
			throw new IllegalStateException("이미 신고한 리뷰입니다.");
		}

		ReviewReport report = new ReviewReport();
		report.setReviewId(reviewId);
		report.setMemberId(memberId);
		report.setReportType(reportType);
		report.setContent(content);
		report.setStatus(ReportStatus.PENDING);

		reportRepository.save(report);
	}
}
