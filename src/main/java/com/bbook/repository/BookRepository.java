package com.bbook.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbook.constant.BookStatus;
import com.bbook.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
	List<Book> findByAuthor(String author);

	List<Book> findByBookStatus(BookStatus bookStatus);

	Page<Book> findByBookStatus(BookStatus bookStatus, Pageable pageable);

	List<Book> findByCategoryId(Long categoryId);

	Page<Book> findByCategoryIdAndBookStatus(
			Long categoryId,
			BookStatus status,
			Pageable pageable);
}
