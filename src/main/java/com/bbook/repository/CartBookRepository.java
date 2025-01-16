package com.bbook.repository;

import java.util.List;

import com.bbook.dto.CartDetailDto;
import com.bbook.entity.CartBook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

public interface CartBookRepository extends JpaRepository<CartBook, Long> {

	CartBook findByCartIdAndBookId(Long cartId, Long bookId);

	@Query("select new com.bbook.dto.CartDetailDto(ci.id, b.title, b.price, ci.count, b.imageUrl, b.stock) " +
			"from CartBook ci " +
			"join ci.book b " +
			"where ci.cart.id = :cartId")
	List<CartDetailDto> findCartDetailDtoList(@Param("cartId") Long cartId);

	long countByCartId(Long cartId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM CartBook ci WHERE ci.id IN :ids")
	void deleteAllByIds(@Param("ids") List<Long> ids);

}