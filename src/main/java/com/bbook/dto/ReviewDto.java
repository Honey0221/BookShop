package com.bbook.dto;

import java.time.LocalDateTime;

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
public class ReviewDto {
	private Long id;
	private Long memberId;
	private Long bookId;
	private int rating;
	private String content;
	private String memberName;
	private LocalDateTime createdAt;
}
