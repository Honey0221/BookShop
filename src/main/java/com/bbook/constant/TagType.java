package com.bbook.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagType {
	THANKS("고마워요"),
	BEST("최고예요"),
	EMPATHY("공감돼요"),
	FUN("재밌어요"),
	HEALING("힐링돼요");

	private final String displayValue;
}
