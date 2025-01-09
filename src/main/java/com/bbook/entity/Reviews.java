package com.bbook.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bbook.constant.TagType;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reviews {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private Long bookId;

	@Column(nullable = false)
	private int rating;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "image_url")
	@ElementCollection
	private List<String> images = new ArrayList<>();

	@Column
	@Enumerated(EnumType.STRING)
	private TagType tagType;

	@Column
	private int likeCount = 0;

	private LocalDateTime createdAt;

	public void updateReview(int rating, String content, TagType tagType) {
		this.rating = rating;
		this.content = content;
		this.tagType = tagType;
	}

	public void addImage(String imageUrl) {
		if (this.images == null) {
			this.images = new ArrayList<>();
		}
		this.images.add(imageUrl);
	}

	public void increaseLikeCount() {
		this.likeCount++;
	}

	public void decreaseLikeCount() {
		this.likeCount--;
	}
}
