package com.bbook.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.bbook.constant.OrderStatus;
import com.bbook.entity.Order;
import com.bbook.dto.OrderSearchDto;
import com.bbook.entity.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

	/**
	 * 특정 이메일로 주문한 모든 주문을 날짜 기준으로 내림차순으로 조회합니다.
	 * 
	 * @param email    회원 이메일
	 * @param pageable 페이징 정보
	 * @return 페이징된 주문 목록
	 */
	@Query("select o from Order o where o.member.email = :email order by o.orderDate desc")
	List<Order> findOrders(@Param("email") String email, Pageable pageable);

	/**
	 * 특정 이메일로 주문한 총 주문 수를 반환합니다.
	 * 
	 * @param email 회원 이메일
	 * @return 총 주문 수
	 */
	@Query("select count(o) from Order o where o.member.email = :email")
	Long countOrder(@Param("email") String email);

	/**
	 * 특정 회원이 특정 책을 특정 상태로 주문했는지 여부를 확인합니다.
	 * 
	 * @param memberId 회원 ID
	 * @param bookId   책 ID
	 * @param status   주문 상태
	 * @return 주문 존재 여부
	 */
	@Query("select count(o) > 0 from Order o " +
			"join o.orderBooks oi where o.member.id = :memberId " +
			"and oi.book.id = :bookId and o.orderStatus = :status")
	boolean existsByMemberIdAndBookIdAndStatus(
			@Param("memberId") Long memberId, @Param("bookId") Long bookId,
			@Param("status") OrderStatus status);

	Optional<Order> findByMerchantUid(String merchantUid);

	@Query("select distinct o from Order o " +
			"join fetch o.member m " +
			"join fetch o.orderBooks ob " +
			"join fetch ob.book b " +
			"where o.orderDate between :startDateTime and :endDateTime " +
			"order by o.orderDate desc")
	List<Order> findByOrderDateBetween(
			@Param("startDateTime") LocalDateTime startDateTime,
			@Param("endDateTime") LocalDateTime endDateTime);

	/**
	 * 관리자 페이지에서 주문을 검색합니다.
	 * 
	 * @param searchDto 검색 조건
	 * @param pageable  페이징 정보
	 * @return 검색된 주문 목록
	 */
	@Query("select o from Order o " +
			"left join o.member m " +
			"left join o.orderBooks ob " +
			"left join ob.book b " +
			"where (:#{#searchDto.searchType} is null or " +
			"(:#{#searchDto.searchType} = 'merchantUid' and o.merchantUid like %:#{#searchDto.searchKeyword}%) or " +
			"(:#{#searchDto.searchType} = 'memberName' and m.nickname like %:#{#searchDto.searchKeyword}%) or " +
			"(:#{#searchDto.searchType} = 'email' and m.email like %:#{#searchDto.searchKeyword}%) or " +
			"(:#{#searchDto.searchType} = 'orderName' and b.title like %:#{#searchDto.searchKeyword}%)) " +
			"and (:#{#searchDto.orderStatusEnum} is null or o.orderStatus = :#{#searchDto.orderStatusEnum}) " +
			"and (:#{#searchDto.startDateTime} is null or o.orderDate >= :#{#searchDto.startDateTime}) " +
			"and (:#{#searchDto.endDateTime} is null or o.orderDate <= :#{#searchDto.endDateTime}) " +
			"group by o.id " +
			"order by o.orderDate desc")
	Page<Order> searchOrders(@Param("searchDto") OrderSearchDto searchDto, Pageable pageable);

	/**
	 * 회원의 주문 내역을 최신순으로 조회합니다.
	 */
	List<Order> findByMemberIdOrderByOrderDateDesc(Long memberId);

	/**
	 * 비슷한 책을 구매한 다른 회원들이 구매한 책을 추천합니다.
	 */
	@Query("SELECT DISTINCT b FROM Book b " +
			"JOIN OrderBook ob ON b = ob.book " +
			"JOIN Order o ON ob.order = o " +
			"WHERE o.member.id IN (" +
			"    SELECT DISTINCT o2.member.id " +
			"    FROM Order o2 " +
			"    JOIN o2.orderBooks ob2 " +
			"    WHERE ob2.book.id IN :bookIds " +
			"    AND o2.member.id != :memberId" +
			") " +
			"AND b.id NOT IN :bookIds " +
			"ORDER BY b.sales DESC")
	List<Book> findCollaborativeBooks(
			@Param("bookIds") List<Long> bookIds,
			@Param("memberId") Long memberId,
			@Param("limit") int limit);
}
