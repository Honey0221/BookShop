package com.bbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryViewCountDto {
	private String category;
	private Long viewCount;
}
