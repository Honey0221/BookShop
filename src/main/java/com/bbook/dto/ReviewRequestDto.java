package com.bbook.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bbook.constant.TagType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewRequestDto {
	private Long bookId;
	private int rating;
	private String content;
	private List<MultipartFile> reviewImages;
	private TagType tagType;
}
