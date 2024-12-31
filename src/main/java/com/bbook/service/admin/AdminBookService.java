package com.bbook.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.entity.Book;
import com.bbook.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookService {
	private final BookRepository bookRepository;

	public Page<Book> getAdminBookPage(Pageable pageable) {
		return bookRepository.findAll(pageable);
	}
}
