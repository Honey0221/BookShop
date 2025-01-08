package com.bbook.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
	PENDING("접수"),
	ACCEPTED("승인"),
	REJECTED("거절");

	private final String displayValue;
}
