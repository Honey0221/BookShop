package com.bbook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbook.entity.WishBook;

public interface WishBookRepository extends JpaRepository<WishBook, Long> {
	Optional<WishBook> findByMemberIdAndBookId(Long memberId, Long bookId);

	// 특정 회원의 특정 도서 찜 여부 확인
	boolean existsByMemberIdAndBookId(Long memberId, Long bookId);

	// 특정 회원의 찜 목록 조회
	List<WishBook> findByMemberIdOrderByIdDesc(Long memberId);

	// 특정 도서의 찜 개수 조회
	Long countByBookId(Long bookId);
}
