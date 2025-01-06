package com.bbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbook.constant.ActivityType;
import com.bbook.entity.MemberActivity;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity, Long> {
	// 활성화된 특정 활동 찾기
	MemberActivity findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalse(
			String memberEmail,
			Long bookId,
			ActivityType activityType);

	// 이미 활성화된 동일한 활동이 있는지 확인
	Optional<MemberActivity> findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalseOrderByActivityTimeDesc(
			String memberEmail,
			Long bookId,
			ActivityType activityType);
}
