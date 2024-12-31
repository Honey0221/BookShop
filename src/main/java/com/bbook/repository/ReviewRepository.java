package com.bbook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbook.dto.ReviewDto;
import com.bbook.entity.Reviews;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {

	@Query("SELECT new com.bbook.dto.ReviewDto(r.id, r.memberId, r.bookId, r.rating, " +
			"r.content, m.nickname, r.createdAt) " +
			"FROM Reviews r " +
			"LEFT JOIN Member m ON r.memberId = m.id " +
			"WHERE r.bookId = :bookId " +
			"ORDER BY r.createdAt DESC")
	Page<ReviewDto> findByBookId(@Param("bookId") Long bookId, Pageable pageable);

	@Query("select coalesce(avg(r.rating), 0.0) from Reviews r where r.bookId = :bookId")
	Double getAverageRatingByBookId(@Param("bookId") Long bookId);

	long countByBookId(Long bookId);
}
