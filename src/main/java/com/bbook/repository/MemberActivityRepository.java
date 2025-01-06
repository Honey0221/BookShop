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
	// 1. 사용자의 카테고리별 활동 통계 조회
	@Query("SELECT ma.mainCategory, COUNT(ma) as count " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.canceled = false " +
			"AND ma.activityTime >= :since " +
			"GROUP BY ma.mainCategory " +
			"ORDER BY count DESC")
	List<Object[]> findCategoryPreferences(
			@Param("email") String email,
			@Param("since") LocalDateTime since);

	// 2. 사용자의 시간대별 활동 패턴 분석
	@Query("SELECT FUNCTION('HOUR', ma.activityTime) as hour, COUNT(ma) " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.canceled = false " +
			"GROUP BY FUNCTION('HOUR', ma.activityTime) " +
			"ORDER BY hour")
	List<Object[]> findHourlyActivityPattern(@Param("email") String email);

	// 3. 특정 카테고리의 인기 도서 조회 (활동 횟수 기준)
	@Query("SELECT ma.bookId, COUNT(ma) as count " +
			"FROM MemberActivity ma " +
			"WHERE ma.mainCategory = :category " +
			"AND ma.canceled = false " +
			"AND ma.activityTime >= :since " +
			"GROUP BY ma.bookId " +
			"ORDER BY count DESC")
	List<Object[]> findPopularBooksByCategory(
			@Param("category") String category,
			@Param("since") LocalDateTime since,
			Pageable pageable);

	// 4. 사용자의 최근 관심 카테고리 조회
	@Query("SELECT DISTINCT ma.mainCategory " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.canceled = false " +
			"AND ma.activityTime >= :since " +
			"ORDER BY ma.activityTime DESC")
	List<String> findRecentCategories(
			@Param("email") String email,
			@Param("since") LocalDateTime since,
			Pageable pageable);

	// 5. 활동 유형별 카테고리 선호도 분석
	@Query("SELECT ma.mainCategory, ma.activityType, COUNT(ma) " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.canceled = false " +
			"AND ma.activityTime >= :since " +
			"GROUP BY ma.mainCategory, ma.activityType " +
			"ORDER BY ma.mainCategory, ma.activityType")
	List<Object[]> analyzeActivityTypesByCategory(
			@Param("email") String email,
			@Param("since") LocalDateTime since);

	// 6. 구매로 이어진 활동 패턴 분석
	@Query("SELECT ma.activityType, COUNT(ma) " +
			"FROM MemberActivity ma " +
			"WHERE ma.memberEmail = :email " +
			"AND ma.bookId = :bookId " +
			"AND ma.canceled = false " +
			"AND ma.activityTime <= " +
			"(SELECT MIN(p.activityTime) FROM MemberActivity p " +
			" WHERE p.memberEmail = :email " +
			" AND p.bookId = :bookId " +
			" AND p.activityType = 'PURCHASE') " +
			"GROUP BY ma.activityType")
	List<Object[]> analyzePurchasePattern(
			@Param("email") String email,
			@Param("bookId") Long bookId);

	// 7. 연관 카테고리 분석 (함께 활동한 카테고리)
	@Query("SELECT ma2.mainCategory, COUNT(DISTINCT ma2.bookId) " +
			"FROM MemberActivity ma1 " +
			"JOIN MemberActivity ma2 ON ma1.memberEmail = ma2.memberEmail " +
			"WHERE ma1.memberEmail = :email " +
			"AND ma1.mainCategory = :category " +
			"AND ma2.mainCategory != :category " +
			"AND ma1.activityTime >= :since " +
			"AND ma2.activityTime >= :since " +
			"AND ma1.canceled = false " +
			"AND ma2.canceled = false " +
			"GROUP BY ma2.mainCategory " +
			"ORDER BY COUNT(DISTINCT ma2.bookId) DESC")
	List<Object[]> findRelatedCategories(
			@Param("email") String email,
			@Param("category") String category,
			@Param("since") LocalDateTime since,
			Pageable pageable);

	// 활성화된 특정 활동 찾기
	MemberActivity findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalse(
			String memberEmail,
			Long bookId,
			ActivityType activityType);

	Optional<MemberActivity> findFirstByMemberEmailAndBookIdAndActivityTypeAndCanceledFalseOrderByActivityTimeDesc(
			String memberEmail,
			Long bookId,
			ActivityType activityType);
}
