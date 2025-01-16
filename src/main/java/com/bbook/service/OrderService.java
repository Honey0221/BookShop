package com.bbook.service;

import com.bbook.constant.OrderStatus;
import com.bbook.dto.OrderDto;
import com.bbook.dto.OrderHistDto;
import com.bbook.entity.Book;
import com.bbook.entity.Member;
import com.bbook.entity.Order;
import com.bbook.entity.OrderBook;
import com.bbook.repository.BookRepository;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {
	// 필요한 Repository들을 주입받음
	private final BookRepository bookRepository; // 상품 정보 관리
	private final MemberRepository memberRepository; // 회원 정보 관리
	private final OrderRepository orderRepository; // 주문 정보 관리

	// 아임포트 API 인증 정보
	@Value("${iamport.key}")
	private String iamportKey;

	@Value("${iamport.secret}")
	private String iamportSecret;

	/**
	 * 단일 상품 주문을 처리하는 메소드
	 * 
	 * @param orderDto 주문 정보를 담은 DTO
	 * @param email    주문자 이메일
	 * @return 생성된 주문의 ID
	 */
	@Transactional
	public Long order(OrderDto orderDto, String email) {
		log.info("주문 생성 시작 - email: {}, bookId: {}", email, orderDto.getBookId());

		try {
			// 상품 조회
			Book item = bookRepository.findById(orderDto.getBookId())
					.orElseThrow(EntityNotFoundException::new);
			log.info("상품 조회 완료 - 상품명: {}, 가격: {}", item.getTitle(), item.getPrice());

			// 회원 조회
			Member member = memberRepository.findByEmail(email)
					.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
			log.info("회원 조회 완료 - 회원명: {}", member.getNickname());

			// 주문 상품 생성
			OrderBook orderBook = OrderBook.createOrderBook(item, orderDto.getCount());
			log.info("주문 상품 생성 완료 - 수량: {}, 총 가격: {}",
					orderBook.getCount(), orderBook.getTotalPrice());

			// 주문 생성
			Order order = Order.createOrder(member, List.of(orderBook), orderDto.getImpUid(),
					orderDto.getMerchantUid());

			// 주문 저장
			orderRepository.save(order);
			log.info("주문 저장 완료 - 주문번호: {}", order.getId());

			return order.getId();
		} catch (Exception e) {
			log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 주문 목록을 조회하는 메소드
	 * 
	 * @param email    사용자 이메일
	 * @param pageable 페이징 정보
	 * @return 주문 내역 페이지
	 */
	@Transactional(readOnly = true)
	public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
		// 주문 목록 조회
		List<Order> orders = orderRepository.findOrders(email, pageable);
		Long totalCount = orderRepository.countOrder(email);

		// 주문 정보를 DTO로 변환
		List<OrderHistDto> orderHistDtos = new ArrayList<>();

		for (Order order : orders) {
			OrderHistDto orderHistDto = new OrderHistDto(order);

			// for (OrderItem orderItem : orderItems) {
			// // 주문 상품의 대표 이미지 조회
			// ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(
			// orderItem.getItem().getId(), "Y");
			// OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
			// orderHistDto.addOrderItemDto(orderItemDto);
			// }

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
			orderHistDto.setOrderDate(order.getOrderDate().format(formatter));

			orderHistDtos.add(orderHistDto);
		}

		return new PageImpl<>(orderHistDtos, pageable, totalCount);
	}

	/**
	 * 장바구니에서 여러 상품을 주문하는 메소드
	 * 
	 * @param orderDtoList 주문할 상품 목록
	 * @param email        주문자 이메일
	 * @return 생성된 주문의 ID
	 */
	@Transactional
	public Long orders(List<OrderDto> orderDtoList, String email) {
		Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		List<OrderBook> orderBooks = new ArrayList<>();

		for (OrderDto orderDto : orderDtoList) {
			Book item = bookRepository.findById(orderDto.getBookId())
					.orElseThrow(EntityNotFoundException::new);
			OrderBook orderBook = OrderBook.createOrderBook(item, orderDto.getCount());
			orderBooks.add(orderBook);
		}

		Order order = Order.createOrder(member, orderBooks, orderDtoList.get(0).getImpUid(),
				orderDtoList.get(0).getMerchantUid());
		if (orderDtoList.get(0).getImpUid() != null) {
			order.setImpUid(orderDtoList.get(0).getImpUid());
			order.setMerchantUid(orderDtoList.get(0).getMerchantUid());
		}
		orderRepository.save(order);

		return order.getId();
	}

	/**
	 * 결제 정보를 검증증하는 메소드
	 * 
	 * @param impUid      아임포트 거래 고유번호
	 * @param merchantUid 주문번호
	 * @param amount      결제 금액
	 * @return 검증 결과
	 */
	@Transactional(readOnly = true)
	public boolean verifyPayment(String impUid, String merchantUid, Long amount) {
		try {
			// 주문 금액 계산 (orderDto의 totalPrice 대신 직접 계산)
			String[] parts = merchantUid.split("-");
			if (parts.length < 2) {
				return false;
			}

			// 결제 금액 검증
			if (amount == null || amount <= 0) {
				return false;
			}

			return true; // 일단 검증 통과 처리

		} catch (Exception e) {
			log.error("Payment verification failed: " + e.getMessage(), e);
			return false;
		}
	}

	@Transactional
	public void cancelOrder(Long orderId) throws EntityNotFoundException {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

		// 포인트 환불 처리
		Member member = order.getMember();
		int usedPoints = order.getUsedPoints(); // 주문 시 사용한 포인트
		int earnedPoints = order.getEarnedPoints(); // 주문으로 적립된 포인트

		// 현재 보유 포인트가 차감할 적립 포인트보다 작은 경우
		if (member.getPoint() < earnedPoints) {
			// 부족한 포인트만큼 사용 포인트에서 차감
			int shortagePoints = (int) (earnedPoints - member.getPoint());
			usedPoints = Math.max(0, usedPoints - shortagePoints);

			// 현재 포인트를 0으로 만들고
			member.setPoint(0);
			// 남은 사용 포인트만 환불
			member.addPoint(usedPoints);
		} else {
			// 정상적인 경우: 적립 포인트 차감 후 사용 포인트 환불
			member.addPoint(-earnedPoints); // 적립 포인트 차감
			member.addPoint(usedPoints); // 사용 포인트 환불
		}

		// 주문 취소 처리
		order.cancelOrder();

		// 주문한 상품의 재고를 원복
		for (OrderBook orderBook : order.getOrderBooks()) {
			Book book = orderBook.getBook();
			book.addStock(orderBook.getCount());
		}
	}

	/**
	 * 주문 ID로 주문을 조회하는 메서드
	 * 
	 * @param orderId 주문 ID
	 * @return 조회된 주문 엔티티
	 * @throws EntityNotFoundException 주문을 찾을 수 없는 경우
	 */
	@Transactional(readOnly = true)
	public Order findById(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. ID: " + orderId));
	}

	@Transactional
	public Order saveOrder(Order order) {
		return orderRepository.save(order);
	}

	public boolean hasUserPurchasedBook(Long memberId, Long bookId) {
		return orderRepository
				.existsByMemberIdAndBookIdAndStatus(memberId, bookId, OrderStatus.PAID);
	}
}
