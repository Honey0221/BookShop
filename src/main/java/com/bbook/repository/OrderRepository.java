package com.bbook.repository;

import java.util.List;
import java.util.Optional;

import com.bbook.constant.OrderStatus;
import com.bbook.entity.Order;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("select o from Order o where o.member.email = :email order by o.orderDate desc")
	List<Order> findOrders(@Param("email") String email, Pageable pageable);

	@Query("select count(o) from Order o where o.member.email = :email")
	Long countOrder(@Param("email") String email);

	Optional<Order> findByMerchantUid(String merchantUid);

	@Query("select count(o) > 0 from Order o " +
	"join o.orderItems oi where o.member.id = :memberId " +
	"and oi.book.id = :bookId and o.orderStatus = :status")
	boolean existsByMemberIdAndBookIdAndStatus(
			@Param("memberId") Long memberId, @Param("bookId") Long bookId,
			@Param("status") OrderStatus status);
}
