package com.bbook.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	// 최근 본 도서 조회
	@Query("SELECT ma FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.activityType = :activityType " +
			"AND ma.canceled = false " +
			"ORDER BY ma.activityTime DESC")
	List<MemberActivity> findRecentActivities(
			@Param("email") String email,
			@Param("activityType") ActivityType activityType,
			Pageable pageable);

	// 1. 컨텐츠 기반 필터링을 위한 쿼리
	@Query("SELECT ma.mainCategory, ma.midCategory, ma.detailCategory, COUNT(ma) " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.canceled = false " +
			"AND ma.activityTime >= :since " +
			"GROUP BY ma.mainCategory, ma.midCategory, ma.detailCategory " +
			"ORDER BY COUNT(ma) DESC")
	List<Object[]> findUserCategoryPreferences(
			@Param("email") String email,
			@Param("since") LocalDateTime since);

	// 2. 협업 필터링을 위한 쿼리
	@Query("SELECT ma2.memberEmail, COUNT(DISTINCT ma2.bookId) " +
			"FROM MemberActivity ma1 " +
			"JOIN MemberActivity ma2 ON ma1.bookId = ma2.bookId " +
			"WHERE ma1.memberEmail = :email " +
			"AND ma2.memberEmail != :email " +
			"AND ma1.canceled = false " +
			"AND ma2.canceled = false " +
			"AND ma1.activityTime >= :since " +
			"GROUP BY ma2.memberEmail " +
			"HAVING COUNT(DISTINCT ma2.bookId) >= :minCommonBooks " +
			"ORDER BY COUNT(DISTINCT ma2.bookId) DESC")
	List<Object[]> findSimilarUsers(
			@Param("email") String email,
			@Param("since") LocalDateTime since,
			@Param("minCommonBooks") long minCommonBooks,
			Pageable pageable);

	// 3. 유사 사용자 기반 도서 추천
	@Query("SELECT ma.bookId, COUNT(DISTINCT ma.memberEmail), " +
			"SUM(CASE " +
			"  WHEN ma.activityType = 'PURCHASE' THEN 3 " +
			"  WHEN ma.activityType = 'REVIEW' THEN 2 " +
			"  WHEN ma.activityType = 'HEART' THEN 1 " +
			"  ELSE 0 END) " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail IN :similarUsers " +
			"AND ma.bookId NOT IN " +
			"    (SELECT ma2.bookId FROM MemberActivity ma2 " +
			"     WHERE ma2.memberEmail = :email) " +
			"AND ma.canceled = false " +
			"GROUP BY ma.bookId " +
			"HAVING COUNT(DISTINCT ma.memberEmail) >= :minUsers " +
			"ORDER BY SUM(CASE " +
			"  WHEN ma.activityType = 'PURCHASE' THEN 3 " +
			"  WHEN ma.activityType = 'REVIEW' THEN 2 " +
			"  WHEN ma.activityType = 'HEART' THEN 1 " +
			"  ELSE 0 END) DESC")
	List<Object[]> findRecommendedBooksBySimilarUsers(
			@Param("email") String email,
			@Param("similarUsers") List<String> similarUsers,
			@Param("minUsers") long minUsers,
			Pageable pageable);

}
