package com.bbook.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbook.entity.Reviews;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
	List<Reviews> findByBookId(Long bookId);

	Page<Reviews> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

	Page<Reviews> findByBookIdOrderByLikeCountDesc(Long bookId, Pageable pageable);

	@Query("select coalesce(avg(r.rating), 0.0) from Reviews r where r.bookId = :bookId")
	Double getAverageRatingByBookId(@Param("bookId") Long bookId);

	long countByBookId(Long bookId);
}
