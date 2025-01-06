package com.bbook.repository;

import java.util.List;

import com.bbook.dto.CartDetailDto;
import com.bbook.entity.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	CartItem findByCartIdAndBookId(Long cartId, Long bookId);

	@Query("select new com.bbook.dto.CartDetailDto(ci.id, b.title, b.price, ci.count, b.imageUrl) " +
			"from CartItem ci " +
			"join ci.book b " +
			"where ci.cart.id = :cartId")
	List<CartDetailDto> findCartDetailDtoList(@Param("cartId") Long cartId);

}