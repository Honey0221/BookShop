package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.ActivityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_activities", indexes = {
		@Index(name = "idx_member_email", columnList = "member_email"),
		@Index(name = "idx_activity_time", columnList = "activity_time")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberActivity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_email")
	private String memberEmail;

	@Enumerated(EnumType.STRING)
	@Column(name = "activity_type", nullable = false)
	private ActivityType activityType;

	@Column(name = "book_id", nullable = false)
	private Long bookId;

	@Column(name = "main_category", length = 50)
	private String mainCategory;

	@Column(name = "mid_category", length = 50)
	private String midCategory;

	@Column(name = "detail_category", length = 50)
	private String detailCategory;

	@Column(name = "activity_time", nullable = false)
	private LocalDateTime activityTime;

	@Column(name = "is_canceled")
	private boolean canceled = false;

	@Column(name = "cancel_time")
	private LocalDateTime cancelTime;

	@Builder
	public MemberActivity(String memberEmail, Long bookId, ActivityType activityType,
			String mainCategory, String midCategory, String detailCategory) {
		this.memberEmail = memberEmail;
		this.bookId = bookId;
		this.activityType = activityType;
		this.mainCategory = mainCategory;
		this.midCategory = midCategory;
		this.detailCategory = detailCategory;
		this.activityTime = LocalDateTime.now();
	}

	public void cancel() {
		if (this.canceled) {
			throw new RuntimeException("이미 취소된 활동입니다.");
		}
		this.canceled = true;
		this.cancelTime = LocalDateTime.now();
	}

	public boolean isCancellable() {
		return !this.canceled &&
				(this.activityType == ActivityType.HEART ||
						this.activityType == ActivityType.CART ||
						this.activityType == ActivityType.PURCHASE ||
						this.activityType == ActivityType.REVIEW);
	}

	public void updateActivityTime(LocalDateTime newTime) {
		this.activityTime = newTime;
	}
}
