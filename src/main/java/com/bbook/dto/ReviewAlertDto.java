package com.bbook.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.bbook.constant.ReportType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAlertDto {
	private Long reviewId;
	private int reportCount;
	private String content;
	private String memberNickname;
	private List<ReportType> reportTypes;
	private LocalDateTime reportedDt;
}
