package com.bbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbook.entity.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
	boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);
}
