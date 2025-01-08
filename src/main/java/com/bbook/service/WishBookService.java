package com.bbook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbook.dto.BookListDto;
import com.bbook.entity.WishBook;
import com.bbook.repository.BookRepository;
import com.bbook.repository.WishBookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WishBookService {
	private final WishBookRepository wishBookRepository;
	private final BookRepository bookRepository;

	// 찜하기/취소하기
	@Transactional
	public boolean toggleWish(Long memberId, Long bookId) {
		// 이미 찜한 상품인지 확인
		System.out.println("4444444444");
		Optional<WishBook> exists = wishBookRepository.findByMemberIdAndBookId(memberId, bookId);
		System.out.println("33333333333");
		if (exists.isPresent()) {
			// 찜 취소
			wishBookRepository.delete(exists.get());
			return false;
		} else {
			// 찜 추가
			WishBook wishBook = WishBook.builder()
					.memberId(memberId)
					.bookId(bookId)
					.build();
			wishBookRepository.save(wishBook);
			return true;
		}
	}

	// 찜 목록 조회
	public List<BookListDto> getWishList(Long memberId) {
		List<WishBook> wishBooks = wishBookRepository.findByMemberIdOrderByIdDesc(memberId);

		return wishBooks.stream()
				.map(wishBook -> bookRepository.findById(wishBook.getBookId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(book -> BookListDto.builder()
						.id(book.getId())
						.title(book.getTitle())
						.author(book.getAuthor())
						.publisher(book.getPublisher())
						.price(book.getPrice())
						.stock(book.getStock())
						.imageUrl(book.getImageUrl())
						.mainCategory(book.getMainCategory())
						.midCategory(book.getMidCategory())
						.subCategory(book.getSubCategory())
						.detailCategory(book.getDetailCategory())
						.description(book.getDescription())
						.bookStatus(book.getBookStatus())
						.createdAt(book.getCreatedAt())
						.viewCount(book.getViewCount())
						.build())
				.toList();
	}

	public boolean isWished(Long memberId, Long bookId) {
		return wishBookRepository.existsByMemberIdAndBookId(memberId, bookId);
	}
}
