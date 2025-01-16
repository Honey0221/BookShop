package com.bbook.dto;

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
public class BookFormDto {
	private String title;
	private String author;
	private String publisher;
	private Integer price;
	private Integer stock;
	private String mainCategory;
	private String midCategory;
	private String subCategory;
	private String detailCategory;
	private String imageUrl;
	private String description;
	private String trailerUrl;

	public String getFullImageUrl() {
		return imageUrl != null ? "/bookshop/book/" + imageUrl : null;
	}
}
