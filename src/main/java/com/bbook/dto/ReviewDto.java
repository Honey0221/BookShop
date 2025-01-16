package com.bbook.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bbook.constant.TagType;
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
	private String bookTitle;
	private int rating;
	private String content;
	private String memberName;
	private TagType tagType;
	private List<MultipartFile> reviewImages; // 업로드용
	private List<String> images; // 조회용
	private int likeCount;
	private boolean isLiked;
	private LocalDateTime createdAt;
	private boolean blocked;

	@JsonProperty("isOwner")
	private boolean isOwner;

	public ReviewDto(Long id, Long memberId, Long bookId, int rating,
			String content, String memberName, TagType tagType,
			List<String> images, int likeCount, LocalDateTime createdAt) {
		this.id = id;
		this.memberId = memberId;
		this.bookId = bookId;
		this.rating = rating;
		this.content = content;
		this.memberName = memberName;
		this.tagType = tagType;
		this.images = images;
		this.likeCount = likeCount;
		this.createdAt = createdAt;
		this.isOwner = false;
	}

	public String getDisplayContent() {
		return blocked ? "클린봇에 의해 차단되었습니다." : content;
	}
}
