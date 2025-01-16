package com.bbook.dto;

import com.bbook.constant.ReportStatus;
import com.bbook.constant.ReportType;
import com.bbook.entity.Book;
import com.bbook.entity.Member;
import com.bbook.entity.ReviewReport;
import com.bbook.entity.Reviews;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReportDto {
	private Long id;
	private Long reviewId;
	private String reviewContent;
	private String bookTitle;
	private String reportContent;
	private String memberNickname;
	private ReportType reportType;

	public static ReviewReportDto from(
			ReviewReport report, Reviews review, Member reporter, Book book) {
		return ReviewReportDto.builder()
				.id(report.getId())
				.reviewId(report.getReviewId())
				.reviewContent(review.getContent())
				.memberNickname(reporter.getNickname())
				.bookTitle(book.getTitle())
				.reportContent(report.getContent())
				.reportType(report.getReportType())
				.build();
	}
}
