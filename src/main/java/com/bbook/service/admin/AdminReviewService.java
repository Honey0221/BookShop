package com.bbook.service.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.constant.ReportStatus;
import com.bbook.dto.ReviewCleanBotDto;
import com.bbook.dto.ReviewDto;
import com.bbook.dto.ReviewReportDto;
import com.bbook.entity.Book;
import com.bbook.entity.Member;
import com.bbook.entity.ReviewReport;
import com.bbook.entity.Reviews;
import com.bbook.repository.BookRepository;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.ReviewReportRepository;
import com.bbook.repository.ReviewRepository;
import com.bbook.service.ReviewReportService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewService {
	private final ReviewReportRepository reportRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewReportService reportService;
	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;

	// 클린봇이 감지한 리뷰 목록 조회
	public Page<ReviewCleanBotDto> getFlaggedReviewsForCleanBot(Pageable pageable) {
		return reviewRepository.findFlaggedReviewsWithBookAndMember(pageable);
	}

	// 리뷰 검색
	public Page<ReviewDto> searchReviews(String searchType, String keyword,
			Pageable pageable) {
		Page<Reviews> reviewsPage = switch (searchType) {
			case "bookTitle" ->
				reviewRepository.findByBook_TitleContaining(keyword, pageable);
			case "content" ->
				reviewRepository.findByContentContaining(keyword, pageable);
			case "memberNickname" ->
				reviewRepository.findByMember_NicknameContaining(keyword, pageable);
			default -> Page.empty(pageable);
		};

		return reviewsPage.map(review -> {
			String memberNickname = memberRepository.findById(review.getMemberId())
					.map(Member::getNickname)
					.orElse("Unknown");
			String bookTitle = bookRepository.findById(review.getBookId())
					.map(Book::getTitle)
					.orElse("삭제된 도서");

			return ReviewDto.builder()
					.id(review.getId())
					.memberId(review.getMemberId())
					.bookId(review.getBookId())
					.bookTitle(bookTitle)
					.rating(review.getRating())
					.content(review.getContent())
					.memberName(memberNickname)
					.tagType(review.getTagType())
					.images(review.getImages())
					.likeCount(review.getLikeCount())
					.createdAt(review.getCreatedAt())
					.build();
		});
	}

	// 신고 리뷰 목록 조회
	public Page<ReviewReportDto> getAllReports(Pageable pageable) {
		return reportRepository.findAll(pageable)
				.map(report -> {
					Member reporter = memberRepository.findById(report.getMemberId())
							.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
					Reviews review = reviewRepository.findById(report.getReviewId())
							.orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
					Book book = null;
					if (review != null) {
						book = bookRepository.findById(review.getBookId()).orElse(null);
					}
					return ReviewReportDto.from(report, review, reporter, book);
				});
	}

	// 전체 신고 수
	public long getReportCount() {
		return reportRepository.count();
	}

	// 리뷰 삭제
	@Transactional
	public void deleteReviews(List<Long> reviewIds) {
		if (reviewIds == null || reviewIds.isEmpty()) {
			return;
		}
		try {
			reviewRepository.deleteAllById(reviewIds);
			System.out.println(reviewIds.size() + "개의 리뷰가 삭제되었습니다.");
		} catch (Exception e) {
			throw new RuntimeException("리뷰 삭제 중 오류 발생 " + e.getMessage());
		}
	}

	@Transactional
	public Long processReport(Long reportId, String status) {
		ReviewReport report = reportRepository.findById(reportId)
				.orElseThrow(() -> new EntityNotFoundException("신고를 찾을 수 없습니다."));

		Long reviewid = report.getReviewId();
		ReportStatus newStatus = ReportStatus.valueOf(status);

		// 같은 리뷰에 대한 모든 신고 조회
		List<ReviewReport> reports = reportRepository.findByReviewId(reviewid);

		// 신고가 승인된 경우 해당 리뷰와 신고 내역도 같이 삭제
		if (newStatus == ReportStatus.ACCEPTED) {
			try {
				reportRepository.deleteAll(reports);
				reviewRepository.deleteById(reviewid);
				System.out.println("리뷰 ID : " + reviewid + "에 대한 " +
						reports.size() + "개의 신고가 승인 처리되어 삭제되었습니다.");
			} catch (Exception e) {
				System.out.println("리뷰 삭제 실패 (이미 삭제되었거나 찾을 수 없음): " +
						e.getMessage());
			}
		}

		// 신고가 거절된 경우 신고만 삭제하고 리뷰는 유지
		if (newStatus == ReportStatus.REJECTED) {
			reportRepository.deleteAll(reports);
			System.out.println("리뷰 ID : " + reviewid + "에 대한 " +
					reports.size() + "개의 신고가 거절 처리되어 삭제되었습니다.");
		}

		return reviewid;
	}
}
