package com.bbook.service;

import org.springframework.stereotype.Service;

import com.bbook.dto.BookListDto;
import com.bbook.entity.Book;
import com.bbook.repository.BookRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class BookListService {

    private final BookRepository bookRepository;

    // 단일 책 조회
    public Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new EntityNotFoundException(
                "책을 찾을 수 없습니다. ID: " + bookId));
    }
    // 카테고리 조회 메서드
    public List<String> getMainCategories() {
        return bookRepository.findDistinctMainCategories();
    }

    public List<String> getMidCategories(String mainCategory) {
        return bookRepository.findDistinctMidCategoriesByMainCategory(mainCategory);
    }

    public List<String> getDetailCategories(String mainCategory, String midCategory) {
        return bookRepository.findDistinctDetailCategoriesByMainAndMidCategory(mainCategory, midCategory);
    }

    // 초기 도서 목록 조회 메서드들
    public List<BookListDto> getInitialBooks(int size) {
        return bookRepository.findTop10ByOrderByIdAsc(PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getInitialBestBooks(int size) {
        return bookRepository.findTop10ByOrderByViewCountDesc(PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getInitialNewBooks(int size) {
        return bookRepository.findTop10ByOrderByCreatedAtDesc(PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 초테고리별 초기 도서 목록 조회
    public List<BookListDto> getInitialBooksByMainCategory(String mainCategory, int size) {
        return bookRepository.findByMainCategoryOrderByIdAsc(mainCategory, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getInitialBooksByCategory(String mainCategory, String midCategory, int size) {
        return bookRepository
                .findByMainCategoryAndMidCategoryOrderByIdAsc(mainCategory, midCategory, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getInitialBooksByDetailCategory(String mainCategory, String midCategory,
            String detailCategory, int size) {
        return bookRepository.findByMainCategoryAndMidCategoryAndDetailCategoryOrderByIdAsc(
                mainCategory, midCategory, detailCategory, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 무한 스크롤을 위한 다음 페이지 로딩 메서드들
    public List<BookListDto> getNextBooks(Long lastId, int size) {
        return bookRepository.findByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getNextBestBooks(Long lastId, int size) {
        return bookRepository.findByIdGreaterThanOrderByViewCountDesc(lastId, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getNextNewBooks(Long lastId, int size) {
        return bookRepository.findByIdGreaterThanOrderByCreatedAtDesc(lastId, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getNextBooksByMainCategory(Long lastId, String mainCategory, int size) {
        return bookRepository
                .findByIdGreaterThanAndMainCategoryOrderByIdAsc(lastId, mainCategory, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getNextBooksByCategory(Long lastId, String mainCategory, String midCategory, int size) {
        return bookRepository.findByIdGreaterThanAndMainCategoryAndMidCategoryOrderByIdAsc(
                lastId, mainCategory, midCategory, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getNextBooksByDetailCategory(Long lastId, String mainCategory, String midCategory,
            String detailCategory, int size) {
        return bookRepository.findByIdGreaterThanAndMainCategoryAndMidCategoryAndDetailCategoryOrderByIdAsc(
                lastId, mainCategory, midCategory, detailCategory, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 검색 관련 메서드
    public List<BookListDto> searchInitialBooks(String searchQuery, int size) {
        return bookRepository.searchInitialBooks(searchQuery, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookListDto> getNextSearchResults(Long lastId, String searchQuery, int size) {
        return bookRepository.findNextSearchResults(lastId, searchQuery, PageRequest.of(0, size))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 정렬 관련 메서드
    public List<BookListDto> sortByPopularity(List<BookListDto> books) {
        return books.stream()
                .sorted((b1, b2) -> b2.getViewCount().compareTo(b1.getViewCount()))
                .collect(Collectors.toList());
    }

    public List<BookListDto> sortByPriceAsc(List<BookListDto> books) {
        return books.stream()
                .sorted(Comparator.comparing(BookListDto::getPrice))
                .collect(Collectors.toList());
    }

    public List<BookListDto> sortByPriceDesc(List<BookListDto> books) {
        return books.stream()
                .sorted((b1, b2) -> b2.getPrice().compareTo(b1.getPrice()))
                .collect(Collectors.toList());
    }

    // DTO 변환 메서드
    private BookListDto convertToDto(Book book) {
        return BookListDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .price(book.getPrice())
                .stock(book.getStock())
                .imageUrl(book.getImageUrl())
                .mainCategory(book.getMainCategory())
                .midCategory(book.getMidCategory())
                .detailCategory(book.getDetailCategory())
                .bookStatus(book.getBookStatus())
                .createdAt(book.getCreatedAt())
                .description(book.getDescription())
                .viewCount(book.getViewCount())
                .build();
    }
}
