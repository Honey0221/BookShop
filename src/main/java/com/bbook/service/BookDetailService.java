package com.bbook.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bbook.entity.Book;
import com.bbook.repository.BookRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookDetailService {
	private final BookRepository bookRepository;

	public List<Book> getBooksByAuthor(String authorName) {
		return bookRepository.findByAuthor(authorName);
	}

	public Book getBookById(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("책이 존재하지 않습니다."));
	}
}
