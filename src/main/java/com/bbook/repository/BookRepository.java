package com.bbook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bbook.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
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
}
