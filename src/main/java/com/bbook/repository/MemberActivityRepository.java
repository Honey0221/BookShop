package com.bbook.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbook.constant.ActivityType;
import com.bbook.entity.MemberActivity;
@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity, Long> {
	// 사용자의 특정 활동 타입 조회 (최신순)
	List<MemberActivity> findByMemberEmailAndActivityTypeOrderByActivityTimeDesc(
			String memberEmail,
			ActivityType activityType,
			Pageable pageable
	);

	// 사용자의 특정 활동 타입 전체 조회
	List<MemberActivity> findByMemberEmailAndActivityType(
			String memberEmail,
			ActivityType activityType
	);

	// 특정 기간 동안의 활동 조회
	List<MemberActivity> findByMemberEmailAndActivityTimeBetweenOrderByActivityTimeDesc(
			String memberEmail,
			LocalDateTime start,
			LocalDateTime end
	);
}
