package com.bbook.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbook.constant.BookStatus;
import com.bbook.dto.CategoryCountDto;
import com.bbook.dto.CategoryViewCountDto;
import com.bbook.entity.Book;

public interface AdminBookRepository extends JpaRepository<Book, Long> {
	// 도서 목록 출력용
	Page<Book> findByTitleContainingAndBookStatus(
			String title, BookStatus status, Pageable pageable);
	Page<Book> findByAuthorContainingAndBookStatus(
			String author, BookStatus status, Pageable pageable);
	Page<Book> findByPublisherContainingAndBookStatus(
			String publisher, BookStatus status, Pageable pageable);
	Page<Book> findByBookStatus(BookStatus status, Pageable pageable);

	// 엑셀 출력용 메서드
	List<Book> findByTitleContainingAndBookStatus(
			String title, BookStatus status, Sort sort);
	List<Book> findByAuthorContainingAndBookStatus(
			String author, BookStatus status, Sort sort);
	List<Book> findByPublisherContainingAndBookStatus(
			String publisher, BookStatus status, Sort sort);
	List<Book> findByBookStatus(BookStatus status, Sort sort);

	// 카테고리 목록
	@Query("select distinct b.mainCategory from Book b "
			+ "where b.mainCategory is not null order by b.mainCategory")
	List<String> findDistinctMainCategories();

	@Query("select distinct b.midCategory from Book b "
			+ "where b.midCategory is not null order by b.midCategory")
	List<String> findDistinctMidCategories();

	@Query("select distinct b.subCategory from Book b "
			+ "where b.subCategory is not null order by b.subCategory")
	List<String> findDistinctSubCategories();

	@Query("select distinct b.detailCategory from Book b "
			+ "where b.detailCategory is not null order by b.detailCategory")
	List<String> findDistinctDetailCategories();

	// 상품 통계 관련
	Long countByBookStatus(BookStatus status);

	@Query("select new com.bbook.dto.CategoryCountDto(b.midCategory, count(b)) " +
	"from Book b group by b.midCategory order by count(b) desc")
	List<CategoryCountDto> countByMidCategory();

	@Query("select case when b.price < 10000 then '1만원 미만' " +
				"when b.price < 20000 then '1~2만원' " +
				"when b.price < 30000 then '2~3만원' " +
				"when b.price < 50000 then '3~5만원' " +
				"else '5만원 이상' " +
				"end as priceRange, " +
				"count(b) as count " +
				"from Book b group by case when b.price < 10000 then '1만원 미만' " +
				"when b.price < 20000 then '1~2만원' " +
				"when b.price < 30000 then '2~3만원' " +
				"when b.price < 50000 then '3~5만원' " +
				"else '5만원 이상' end order by min(b.price)")
	List<Object[]> findBooksByPriceRange();

	List<Book> findTop5ByOrderByStockAsc();

	List<Book> findTop3ByOrderByViewCountDesc();

	@Query("select new com.bbook.dto.CategoryViewCountDto(b.midCategory, sum(b.viewCount)) " +
			"from Book b group by b.midCategory order by sum(b.viewCount) desc")
	List<CategoryViewCountDto> findFirst3MidCategoriesByViewCount(Pageable pageable);
}
