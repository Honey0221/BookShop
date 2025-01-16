package com.bbook.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bbook.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
	List<Book> findByAuthor(String author);
	List<Book> findByMidCategory(String midCategory);

	@Query("SELECT DISTINCT b.mainCategory FROM Book b WHERE b.mainCategory IS NOT NULL ORDER BY b.mainCategory")
	List<String> findDistinctMainCategories();

	@Query("SELECT DISTINCT b.midCategory FROM Book b WHERE b.mainCategory = :mainCategory AND b.midCategory IS NOT NULL ORDER BY b.midCategory")
	List<String> findDistinctMidCategoriesByMainCategory(@Param("mainCategory") String mainCategory);

	@Query("SELECT DISTINCT b.detailCategory FROM Book b WHERE b.mainCategory = :mainCategory AND b.midCategory = :midCategory AND b.detailCategory IS NOT NULL ORDER BY b.detailCategory")
	List<String> findDistinctDetailCategoriesByMainAndMidCategory(
			@Param("mainCategory") String mainCategory,
			@Param("midCategory") String midCategory);

	List<Book> findByMainCategory(String mainCategory);

	List<Book> findByMainCategoryAndMidCategory(String mainCategory, String midCategory);

	List<Book> findByMainCategoryAndMidCategoryAndDetailCategory(
			String mainCategory,
			String midCategory,
			String detailCategory);

	List<Book> findTop10ByOrderByIdAsc(Pageable pageable);

	List<Book> findTop15ByIdGreaterThanEqualOrderByIdAsc(Long id);

	List<Book> findTop10ByOrderByViewCountDesc(Pageable pageable);

	List<Book> findTop10ByOrderByCreatedAtDesc(Pageable pageable);

	@Query("SELECT b FROM Book b WHERE b.id > :lastId AND (b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.publisher LIKE %:keyword%) ORDER BY b.id ASC")
	List<Book> findNextSearchResults(
			@Param("lastId") Long lastId,
			@Param("keyword") String keyword,
			Pageable pageable);

	List<Book> findByIdGreaterThanOrderByIdAsc(Long lastId, Pageable pageable);

	List<Book> findByIdGreaterThanAndMainCategoryOrderByIdAsc(Long lastId, String mainCategory, Pageable pageable);

	List<Book> findByIdGreaterThanAndMainCategoryAndMidCategoryOrderByIdAsc(
			Long lastId, String mainCategory, String midCategory, Pageable pageable);

	List<Book> findByIdGreaterThanAndMainCategoryAndMidCategoryAndDetailCategoryOrderByIdAsc(
			Long lastId, String mainCategory, String midCategory, String detailCategory, Pageable pageable);

	List<Book> findByIdGreaterThanOrderByViewCountDesc(Long lastId, Pageable pageable);

	List<Book> findByIdGreaterThanOrderByCreatedAtDesc(Long lastId, Pageable pageable);

	@Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.publisher LIKE %:keyword%")
	List<Book> searchInitialBooks(@Param("keyword") String keyword, Pageable pageable);

	List<Book> findByMainCategoryOrderByIdAsc(String mainCategory, Pageable pageable);

	List<Book> findByMainCategoryAndMidCategoryOrderByIdAsc(
			String mainCategory,
			String midCategory,
			Pageable pageable);

	List<Book> findByMainCategoryAndMidCategoryAndDetailCategoryOrderByIdAsc(
			String mainCategory,
			String midCategory,
			String detailCategory,
			Pageable pageable);

	@Query("SELECT b FROM Book b WHERE " +
			"b.mainCategory = :mainCategory OR " +
			"b.midCategory = :midCategory OR " +
			"b.detailCategory = :detailCategory")
	List<Book> findByMainCategoryOrMidCategoryOrDetailCategory(
			@Param("mainCategory") String mainCategory,
			@Param("midCategory") String midCategory,
			@Param("detailCategory") String detailCategory);

	// 조회수 기반 베스트 도서 조회
	@Query("SELECT b FROM Book b ORDER BY b.viewCount DESC")
	List<Book> findTop15ByOrderByViewCountDesc(Pageable pageable);

	// 최신 도서 조회 (등록일 기준)
	@Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
	List<Book> findTop15ByOrderByCreatedAtDesc(Pageable pageable);

	// 카테고리별 인기 도서 조회
	@Query("SELECT b FROM Book b WHERE " +
			"b.mainCategory = :category OR " +
			"b.midCategory = :category OR " +
			"b.detailCategory = :category " +
			"ORDER BY b.viewCount DESC")
	List<Book> findTopBooksByCategory(@Param("category") String category, Pageable pageable);

	default List<Book> findTopBooksByCategory(String category, int limit) {
		return findTopBooksByCategory(category, PageRequest.of(0, limit));
	}

}
