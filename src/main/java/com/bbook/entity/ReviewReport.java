package com.bbook.entity;

import com.bbook.constant.ReportStatus;
import com.bbook.constant.ReportType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReviewReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long reviewId;

	@Column(nullable = false)
	private Long memberId; // 신고자 -> id를 통해 getNickname()

	@Enumerated(EnumType.STRING)
	private ReportType reportType;

	private String content;

	@Enumerated(EnumType.STRING)
	private ReportStatus status = ReportStatus.PENDING;
}
