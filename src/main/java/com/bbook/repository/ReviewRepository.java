package com.bbook.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbook.dto.ReviewCleanBotDto;
import com.bbook.entity.Reviews;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
	List<Reviews> findByBookId(Long bookId);

	// 리뷰 정렬
	Page<Reviews> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);
	Page<Reviews> findByBookIdOrderByLikeCountDesc(Long bookId, Pageable pageable);

	// 리뷰 검색
	@Query("select r from Reviews r join Book b on r.bookId = b.id " +
	"where b.title like %:title%")
	Page<Reviews> findByBook_TitleContaining(@Param("title") String title, Pageable pageable);
	Page<Reviews> findByContentContaining(String content, Pageable pageable);
	@Query("select r from Reviews r join Member m on r.memberId = m.id " +
	"where m.nickname like %:nickname%")
	Page<Reviews> findByMember_NicknameContaining(@Param("nickname") String nickname, Pageable pageable);

	@Query("select coalesce(avg(r.rating), 0.0) from Reviews r where r.bookId = :bookId " +
	"and r.blocked = false")
	Double getAverageRatingByBookId(@Param("bookId") Long bookId);

	@Query("select count(r) from Reviews r where r.bookId = :bookId and r.blocked = false")
	long countValidReviewsByBookId(@Param("bookId") Long bookId);

	// 악플이나 불쾌한 리뷰만 조회 (책, 회원 정보 포함)
	@Query("select new com.bbook.dto.ReviewCleanBotDto(r.id, b.title, m.nickname, r.rating, " +
			"r.content, r.createdAt, r.blocked, r.flagged) from Reviews r " +
			"join Book b on r.bookId = b.id join Member m on r.memberId = m.id " +
			"where r.blocked = true or r.flagged = true " +
			"order by r.createdAt desc")
	Page<ReviewCleanBotDto> findFlaggedReviewsWithBookAndMember(Pageable pageable);
}
