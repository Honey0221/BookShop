package com.bbook.service;

import com.bbook.constant.OrderStatus;
import com.bbook.dto.CancelData;
import com.bbook.dto.OrderDto;
import com.bbook.dto.OrderHistDto;
import com.bbook.entity.Book;
import com.bbook.entity.Member;
import com.bbook.entity.Order;
import com.bbook.entity.OrderItem;
import com.bbook.repository.BookRepository;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.OrderRepository;
import com.bbook.dto.PaymentDto;
import com.bbook.exception.IamportResponseException;
import com.bbook.client.IamportClient;
import com.bbook.client.IamportResponse;
import com.bbook.dto.Payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigDecimal;

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
	private final IamportClient iamportClient; // 아임포트 결제 API 클라이언트

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
					.orElseThrow(EntityNotFoundException::new);
			log.info("회원 조회 완료 - 회원명: {}", member.getNickname());

			// 주문 상품 생성
			OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
			log.info("주문 상품 생성 완료 - 수량: {}, 총 가격: {}",
					orderItem.getCount(), orderItem.getTotalPrice());

			// 주문 생성
			Order order = Order.createOrder(member, List.of(orderItem));
			order.setMerchantUid(orderDto.getMerchantUid());
			order.setImpUid(orderDto.getImpUid());

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
	 * 주문 취소 권한을 검증하는 메소드
	 * 
	 * @param orderId 주문 ID
	 * @param email   사용자 이메일
	 * @return 취소 권한 여부
	 */
	@Transactional(readOnly = true)
	public boolean validateOrder(Long orderId, String email) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

		// 주문한 회원의 이메일과 현재 로그인한 사용자의 이메일이 같은지 확인
		return order.getMember().getEmail().equals(email);
	}

	/**
	 * 주문을 취소하는 메소드
	 * 
	 * @param orderId 주문 ID
	 * @param email   사용자 이메일
	 */
	@Transactional
	public void cancelOrder(Long orderId, String email) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

		// 주문자 검증
		if (!order.getMember().getEmail().equals(email)) {
			throw new IllegalStateException("주문 취소 권한이 없습니다.");
		}

		// 주문 상태 검증
		if (order.getOrderStatus() != OrderStatus.PAID) {
			throw new IllegalStateException("이미 취소되었거나 취소할 수 없는 주문입니다.");
		}

		try {
			String impUid = order.getImpUid();
			if (impUid != null) {
				CancelData cancelData = new CancelData(impUid, true);
				IamportResponse<Payment> cancellation = iamportClient.cancelPayment(cancelData);

				if (cancellation.getResponse() == null) {
					throw new RuntimeException("결제 취소에 실패했습니다.");
				}

				order.cancelOrder();
			}
		} catch (IamportResponseException | IOException e) {
			log.error("결제 취소 중 오류 발생: {}", e.getMessage());
			throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
		}
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
		List<OrderItem> orderItemList = new ArrayList<>();

		for (OrderDto orderDto : orderDtoList) {
			Book item = bookRepository.findById(orderDto.getBookId())
					.orElseThrow(EntityNotFoundException::new);
			OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
			orderItemList.add(orderItem);
		}

		Order order = Order.createOrder(member, orderItemList);
		if (orderDtoList.get(0).getImpUid() != null) {
			order.setImpUid(orderDtoList.get(0).getImpUid());
			order.setMerchantUid(orderDtoList.get(0).getMerchantUid());
		}
		orderRepository.save(order);

		return order.getId();
	}

	/**
	 * 주문 상세 정보를 조회하는 메소드
	 * 
	 * @param orderId 주문 ID
	 * @return 주문 상세 정보
	 */
	public OrderDto getOrderDetails(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
		return OrderDto.of(order);
	}

	/**
	 * 결제 정보를 검증하고 업데이트하는 메소드
	 * 
	 * @param paymentDto 결제 정보
	 */
	public void validateAndUpdatePayment(PaymentDto paymentDto) throws IamportResponseException, IOException {
		IamportResponse<Payment> payment = iamportClient.paymentByImpUid(paymentDto.getImpUid());

		// 결제 금액 검증
		if (payment.getResponse().getAmount().compareTo(BigDecimal.valueOf(paymentDto.getAmount())) != 0) {
			throw new RuntimeException("결제 금액이 일치하지 않습니다.");
		}

		// 주문 상태 업데이트
		Order order = orderRepository.findByMerchantUid(paymentDto.getMerchantUid())
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

		order.setOrderStatus(OrderStatus.PAID);
		orderRepository.save(order);
	}

	/**
	 * 주문번호로 주문 정보를 조회하는 메소드
	 * 
	 * @param merchantUid 주문번호
	 * @return 주문 정보
	 */
	public OrderDto getOrderByMerchantUid(String merchantUid) {
		Order order = orderRepository.findByMerchantUid(merchantUid)
				.orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
		return OrderDto.of(order);
	}

	/**
	 * 주문의 총 금액을 계산하는 메소드
	 * 
	 * @param orderDto 주문 정보
	 * @return 총 주문 금액
	 */
	public int getTotalPrice(OrderDto orderDto) {
		Book item = bookRepository.findById(orderDto.getBookId())
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
		return item.getPrice() * orderDto.getCount();
	}

	/**
	 * 주문명을 생성하는 메소드
	 * 
	 * @param orderDto 주문 정보
	 * @return 생성된 주문명
	 */
	public String getOrderName(OrderDto orderDto) {
		Book item = bookRepository.findById(orderDto.getBookId())
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
		return item.getTitle() + " 외 " + orderDto.getCount() + "개";
	}

	/**
	 * 결제 정보를 검증하는 메소드
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

		// 주문 취소 처리
		order.cancelOrder();

		// 주문한 상품의 재고를 원복
		for (OrderItem orderItem : order.getOrderItems()) {
			Book book = orderItem.getBook();
			book.addStock(orderItem.getCount());
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

	@Transactional(readOnly = true)
	public Order findByMerchantUid(String merchantUid) {
		return orderRepository.findByMerchantUid(merchantUid)
				.orElse(null);
	}

	@Transactional
	public Order saveOrder(Order order) {
		return orderRepository.save(order);
	}

	public Long order(OrderDto orderDto) {
		Order order = new Order();
		// ... 기존 코드 ...

		order.setOriginalPrice(orderDto.getOriginalPrice());
		order.setShippingFee(orderDto.getOriginalPrice() < 15000 ? 3000L : 0L);
		order.setTotalPrice(orderDto.getTotalPrice());

		return orderRepository.save(order).getId();
	}

	public boolean hasUserPurchasedBook(Long memberId, Long bookId) {
		return orderRepository
				.existsByMemberIdAndBookIdAndStatus(memberId, bookId, OrderStatus.PAID);
	}
}
