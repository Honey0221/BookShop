package com.bbook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbook.entity.ReviewLike;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
	// 특정 리뷰에 대한 특정 회원의 좋아요 조회
	Optional<ReviewLike> findByReviewIdAndMemberId(Long reviewId, Long memberId);

	// 특정 리뷰에 대한 특정 회원의 좋아요 여부 확인
	boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

	// 특정 리뷰의 전체 좋아요 수 조회
	@Query("select count(rl) from ReviewLike rl where rl.reviewId = :reviewId")
	int countByReviewId(@Param("reviewId") Long reviewId);

	// 특정 회원이 좋아요한 리뷰 ID 목록 조회
	List<Long> findReviewIdsByMemberId(Long memberId);
}
