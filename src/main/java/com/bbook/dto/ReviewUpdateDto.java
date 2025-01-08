package com.bbook.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bbook.constant.TagType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateDto {
	private int rating;
	private String content;
	private List<MultipartFile> reviewImages;
	private TagType tagType;
}
