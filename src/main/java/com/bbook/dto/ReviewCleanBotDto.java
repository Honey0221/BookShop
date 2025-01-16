package com.bbook.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCleanBotDto {
	private Long id;
	private String bookTitle;
	private String memberNickname;
	private int rating;
	private String content;
	private LocalDateTime createdAt;
	private boolean blocked;
	private boolean flagged;
}
