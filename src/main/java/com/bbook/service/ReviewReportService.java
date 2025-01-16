package com.bbook.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.constant.ReportStatus;
import com.bbook.dto.ReviewAlertDto;
import com.bbook.entity.ReviewReport;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.ReviewReportRepository;
import com.bbook.repository.ReviewRepository;
import com.bbook.service.admin.TelegramAlertService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewReportService {
	// 누적 신고 기준 수
	private static final int ALERT_THRESHOLD = 5;

	private final ReviewReportRepository reportRepository;
	private final TelegramAlertService telegramAlertService;
	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;

	// 신고 누적 리뷰 체크 및 알림 발송
	@Transactional(readOnly = true)
	public void checkReportAlerts() {
		System.out.println("리뷰 누적 신고 수 체크중...");
		List<Object[]> reportedReviews =
				reportRepository.findReviewIdsWithReportCountOverThreshold(ALERT_THRESHOLD);

		for (Object[] row : reportedReviews) {
			Long reviewId = (Long) row[0];
			Long reportCount = (Long) row[1];

			try {
				reviewRepository.findById(reviewId).ifPresent(review -> {
					memberRepository.findById(review.getMemberId()).ifPresent(member -> {
						ReviewAlertDto alertDto = ReviewAlertDto.builder()
								.reviewId(reviewId)
								.reportCount(reportCount.intValue())
								.content(review.getContent())
								.memberNickname(member.getNickname())
								.build();

						telegramAlertService.sendReportAlert(alertDto);
						System.out.println("신고 알림 발송 완료 reviewId : " + reviewId +
								" reportCount : " + reportCount);
					});
				});
			} catch (Exception e) {
				System.out.println("오류 발생 : " + e.getMessage());
			}
		}
	}

	// 특정 리뷰의 신고 현황 조회
	public int getReportCount(Long reviewId) {
		return reportRepository.countPendingReportsByReviewId(reviewId);
	}

	// 특정 리뷰의 신고 목록 조회
	public List<ReviewReport> getReviewReports(Long reviewId) {
		return reportRepository
				.findByReviewIdAndStatusOrderByIdDesc(reviewId, ReportStatus.PENDING);
	}
}
