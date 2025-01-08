package com.bbook.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
	SPAM("스팸/광고"),
	INAPPROPRIATE("부적절한 내용"),
	HATE_SPEECH("혐오 발언"),
	FALSE_INFO("허위 정보"),
	OTHER("기타");

	private final String displayValue;
}
