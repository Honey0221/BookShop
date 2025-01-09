package com.bbook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bbook.entity.Book;
import com.bbook.repository.BookRepository;
import com.bbook.service.crawling.BookTrailerCrawler;
import com.bbook.utils.YoutubeUrlConverter;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookDetailService {
	private final BookRepository bookRepository;
	private final YoutubeUrlConverter youtubeUrlConverter;
	private final BookTrailerCrawler trailerCrawler;

	public List<Book> getBooksByAuthor(String authorName) {
		return bookRepository.findByAuthor(authorName);
	}

	public Book getBookById(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("책이 존재하지 않습니다."));
	}

	@Transactional
	public void incrementViewCount(Long bookId) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
		book.setViewCount(book.getViewCount() + 1);
	}

	@Async // 비동기 실행
	@Transactional
	public void getBookTrailerUrl(Long bookId) {
		try {
			Book book = bookRepository.findById(bookId)
					.orElseThrow(() -> new EntityNotFoundException("책이 존재하지 않습니다."));

			System.out.println("북 트레일러 크롤링 시작 - " + book.getTitle());
			String trailerUrl = trailerCrawler.getBookTrailerUrl(book.getTitle());
			if (trailerUrl != null && !trailerUrl.isEmpty()) {
				String embedUrl = youtubeUrlConverter.convertToEmbedUrl(trailerUrl);
				book.setTrailerUrl(embedUrl);
				System.out.println("북 트레일러 URL 업데이트 완료");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public List<Book> getBooksByMidCategory(String midCategory) {
		return bookRepository.findByMidCategory(midCategory);
	}
}
