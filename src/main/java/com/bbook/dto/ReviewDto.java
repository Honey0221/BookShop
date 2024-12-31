package com.bbook.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonProperty("isOwner")
	private boolean isOwner;

	public ReviewDto(Long id, Long memberId, Long bookId, int rating,
			String content, String memberName, LocalDateTime createdAt) {
		this.id = id;
		this.memberId = memberId;
		this.bookId = bookId;
		this.rating = rating;
		this.content = content;
		this.memberName = memberName;
		this.createdAt = createdAt;
		this.isOwner = false;
	}
}
