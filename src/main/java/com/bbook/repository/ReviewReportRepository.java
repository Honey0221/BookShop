package com.bbook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbook.constant.ReportStatus;
import com.bbook.entity.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
	boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

	// 특정 리뷰의 PENDING 상태인 신고 개수 조회
	@Query("select count(r) from ReviewReport r " +
			"where r.reviewId = :reviewId and r.status = 'PENDING'")
	int countPendingReportsByReviewId(Long reviewId);

	// 특정 리뷰의 PENDING 상태인 신고 목록 조회
	List<ReviewReport> findByReviewIdAndStatusOrderByIdDesc(Long reviewId, ReportStatus status);

	// PENDING 상태인 신고가 N회 이상인 리뷰 ID 목록 조회
	@Query("select r.reviewId, count(r) as cnt from ReviewReport r " +
			"where r.status = 'PENDING' " +
			"group by r.reviewId " +
			"having count(r) >= :threshold")
	List<Object[]> findReviewIdsWithReportCountOverThreshold(int threshold);

	List<ReviewReport> findByReviewId(Long reviewid);
}
