package com.bbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.bbook.dto.CartDetailDto;
import com.bbook.dto.CartBookDto;
import com.bbook.dto.CartOrderDto;
import com.bbook.dto.OrderDto;
import com.bbook.dto.OrderBookDto;
import com.bbook.entity.Book;
import com.bbook.entity.Cart;
import com.bbook.entity.CartBook;
import com.bbook.entity.Member;
import com.bbook.entity.Order;
import com.bbook.entity.OrderBook;
import com.bbook.repository.CartBookRepository;
import com.bbook.repository.CartRepository;
import com.bbook.repository.BookRepository;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.OrderRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private static final Logger log = LoggerFactory.getLogger(CartService.class);

	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;
	private final CartRepository cartRepository;
	private final CartBookRepository cartBookRepository;
	private final OrderRepository orderRepository;

	public Long addCart(CartBookDto cartBookDto, String email) {
		// Item 객체 DB애서 추출
		Book book = bookRepository.findById(cartBookDto.getBookId())
				.orElseThrow(EntityNotFoundException::new);
		// Member 갹채 DB애소 추출
		Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		// member ID를 통해서 Cart 객체 추출
		Cart cart = cartRepository.findByMemberId(member.getId());
		// Cart 객체가 null이면 Cart 객채 생성 <-> 현재 로그인된 Member
		if (cart == null) {
			cart = Cart.createCart(member);
			cartRepository.save(cart);
		}
		// Cart ID와 Itme ID를 넣어서 CartItem 객체를 추출
		CartBook savedCartBook = cartBookRepository.findByCartIdAndBookId(cart.getId(),
				book.getId());
		// 추출된 CartItem 객체가 있으면
		if (savedCartBook != null) {
			savedCartBook.addCount(cartBookDto.getCount()); // 있는 객체에 수량 증가
			return savedCartBook.getId();
			// 추출된 CartItem 객체가 없으면
		} else {
			// CartItem 객체를 생성하고 save를 통해 DB에 저장
			CartBook cartBook = CartBook.createCartBook(cart, book,
					cartBookDto.getCount());
			cartBookRepository.save(cartBook);
			return cartBook.getId();
		}
	}

	/**
	 * 장바구니 목록을 조회하는 메소드
	 * 
	 * @param email 사용자 이메일
	 * @return 장바구니 상세 정보 목록
	 */
	@Transactional(readOnly = true)
	public List<CartDetailDto> getCartList(String email) {
		// 장바구니 상세 정보를 담을 리스트 초기화
		List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

		// 사용자 정보 조회
		Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);

		// 사용자의 장바구니 조회
		Cart cart = cartRepository.findByMemberId(member.getId());

		// 장바구니가 없는 경우 빈 리스트 반환
		if (cart == null) {
			return cartDetailDtoList;
		}

		// 장바구니에 담긴 상품 상세 정보 조회
		cartDetailDtoList = cartBookRepository.findCartDetailDtoList(cart.getId());

		return cartDetailDtoList;
	}

	@Transactional(readOnly = true)
	public boolean validateCartBook(Long cartBookId, String email) {
		// email을 이용해서 Member 엔티티 객체 추출
		Member curMember = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		// cartItemId를 이용해서 cartItem 엔티티 객체 추출
		CartBook cartBook = cartBookRepository.findById(cartBookId)
				.orElseThrow(EntityExistsException::new);
		// Cart -> Member 엔티티 객체를 추출
		Member savedMember = cartBook.getCart().getMember();
		// 현재 로그인 된 Member == CartItem에 있는 Member -> 같지 않으면 true return false
		if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
			return false;
		}
		// 현재 로그인 된 Member == CartItem에 있는 Memeber -> 같으면 return true
		return true;
	}

	/**
	 * 장바구니 상품의 수량을 업데이트하는 메소드
	 * 
	 * @param cartBookId 장바구니 상품 ID
	 * @param count      변경할 수량
	 * @throws EntityExistsException 장바구니 상품을 찾을 수 없는 경우
	 */
	@Transactional
	public void updateCartBookCount(Long cartBookId, int count) {
		CartBook cartBook = cartBookRepository.findById(cartBookId)
				.orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다."));
		cartBook.updateCount(count);
	}

	/**
	 * 장바구니에서 상품을 삭제하는 메소드
	 * 
	 * @param cartBookId 삭제할 장바구니 상품 ID
	 * @throws EntityExistsException 장바구니 상품을 찾을 수 없는 경우
	 */

	@Transactional
	public Long deleteCartBook(Long cartBookId) {
		CartBook cartBook = cartBookRepository.findById(cartBookId)
				.orElseThrow(EntityExistsException::new);
		Long bookId = cartBook.getBook().getId();
		cartBookRepository.delete(cartBook);
		return bookId;
	}

	@Transactional
	public void deleteCartBooks(List<CartOrderDto> cartOrderDtoList, String email) {
		try {
			log.info("장바구니 삭제 시작 - 이메일: {}, 상품 개수: {}", email, cartOrderDtoList.size());

			// 1. 회원의 장바구니 조회
			Member member = memberRepository.findByEmail(email)
					.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
			Cart cart = cartRepository.findByMemberId(member.getId());

			if (cart == null) {
				log.warn("장바구니가 존재하지 않습니다. - 회원: {}", email);
				return;
			}

			// 2. 장바구니 아이템 ID 목록 수집
			List<Long> cartBookIds = cartOrderDtoList.stream()
					.map(CartOrderDto::getCartBookId)
					.collect(Collectors.toList());
			log.info("삭제할 장바구니 아이템 ID: {}", cartBookIds);

			// 3. CartItems 조회 및 Cart 관계 제거
			List<CartBook> cartBooks = cartBookRepository.findAllById(cartBookIds);
			for (CartBook cartBook : cartBooks) {
				// Cart와 CartItem의 양방향 관계 해제
				cart.getCartBooks().remove(cartBook);
				cartBook.setCart(null);
			}
			cartRepository.saveAndFlush(cart);

			// 4. CartItems 삭제
			for (CartBook cartBook : cartBooks) {
				cartBookRepository.delete(cartBook);
			}
			cartBookRepository.flush();

			log.info("장바구니 아이템 삭제 처리 완료");
		} catch (Exception e) {
			log.error("장바구니 삭제 중 오류 발생: {}", e.getMessage(), e);
			throw e;
		}
	}

	@Transactional
	public Long orderCartBook(List<CartOrderDto> cartOrderDtoList, String email,
			String impUid, String merchantUid, int usedPoints, int discountAmount) {
		long totalAmount = 0;
		List<OrderBook> orderBooks = new ArrayList<>();
		Map<Long, CartBook> cartBooks = new HashMap<>(); // 장바구니 아이템들을 Map으로 관리

		// 1. 먼저 모든 장바구니 아이템을 조회하고 검증
		for (CartOrderDto cartOrderDto : cartOrderDtoList) {
			CartBook cartBook = cartBookRepository.findById(cartOrderDto.getCartBookId())
					.orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다."));
			cartBooks.put(cartBook.getId(), cartBook);

			// 각 상품의 가격 계산
			long bookPrice = cartBook.getBook().getPrice() * cartBook.getCount();
			totalAmount += bookPrice;

			log.info("장바구니 상품 정보 - 상품: {}, 가격: {}, 수량: {}, 금액: {}",
					cartBook.getBook().getTitle(),
					cartBook.getBook().getPrice(),
					cartBook.getCount(),
					bookPrice);
		}

		log.info("전체 장바구니 상품 수: {}, 총 주문 금액: {}", cartBooks.size(), totalAmount);

		// 2. 모든 장바구니 아이템을 OrderItem으로 변환
		for (CartBook cartBook : cartBooks.values()) {
			OrderBook orderBook = OrderBook.createOrderBook(cartBook.getBook(), cartBook.getCount());
			orderBooks.add(orderBook);
		}

		// 3. 하나의 Order로 모든 OrderItem을 묶음
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

		Order order = Order.createOrder(member, orderBooks, impUid, merchantUid);
		order.setOriginalPrice(totalAmount); // 전체 상품의 원래 가격

		// 포인트와 쿠폰 할인 정보 설정
		order.setUsedPoints(usedPoints);
		order.setDiscountAmount(discountAmount);
		order.setIsCouponUsed(discountAmount > 0);

		// 최종 금액 계산 (원래 가격 - 포인트 사용 - 쿠폰 할인)
		long finalPrice = totalAmount - usedPoints - discountAmount;
		order.setTotalPrice(finalPrice);

		// 4. Order 저장
		orderRepository.save(order);
		orderRepository.flush();

		log.info("주문 생성 완료 - 주문 ID: {}, 상품 수: {}, 원래 금액: {}, 최종 금액: {}",
				order.getId(), orderBooks.size(), totalAmount, finalPrice);

		return order.getId();
	}

	@Transactional(readOnly = true)
	public OrderDto createTempOrderInfo(List<CartOrderDto> cartOrderDtoList, String email) {
		Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		List<CartBook> cartBooks = new ArrayList<>();

		// 장바구니 상품 정보 조회
		for (CartOrderDto cartOrderDto : cartOrderDtoList) {
			CartBook cartBook = cartBookRepository.findById(cartOrderDto.getCartBookId())
					.orElseThrow(EntityNotFoundException::new);
			cartBooks.add(cartBook);
		}

		// 주문 정보 생성
		OrderDto orderDto = new OrderDto();
		Long originalPrice = calculateTotalPrice(cartBooks); // 순수 상품 금액
		Long totalPrice = originalPrice; // 최종 결제 금액 (배송비는 프론트에서 계산)

		// 주문 정보 설정
		orderDto.setOrderName(createOrderName(cartBooks));
		orderDto.setOriginalPrice(originalPrice); // 순수 상품 금액 설정
		orderDto.setTotalPrice(totalPrice); // 최종 결제 금액 설정
		orderDto.setCount(calculateTotalCount(cartBooks));
		orderDto.setEmail(email);
		orderDto.setName(member.getName());
		orderDto.setPhone(member.getPhone());
		orderDto.setAddress(member.getAddress());
		orderDto.setIsCouponUsed(false); // 초기값 설정
		orderDto.setUsedPoints(0); // 초기값 설정
		orderDto.setDiscountAmount(0); // 초기값 설정

		// 주문 상품 목록 설정
		for (CartBook cartBook : cartBooks) {
			Book book = cartBook.getBook();
			OrderBookDto orderBookDto = new OrderBookDto(
					OrderBook.createOrderBook(book, cartBook.getCount()),
					book.getImageUrl());
			orderDto.getOrderBookDtoList().add(orderBookDto);
		}

		// 첫 번째 상품의 대표 이미지 URL 설정
		if (!cartBooks.isEmpty()) {
			Book book = cartBooks.get(0).getBook();
			orderDto.setImageUrl(book.getImageUrl());
			orderDto.setBookId(book.getId());
		}

		return orderDto;
	}

	private String createOrderName(List<CartBook> cartBooks) {
		String firstBookName = cartBooks.get(0).getBook().getTitle();
		if (cartBooks.size() > 1) {
			return firstBookName + " 외 " + (cartBooks.size() - 1) + "건";
		}
		return firstBookName;
	}

	private Long calculateTotalPrice(List<CartBook> cartBooks) {
		return cartBooks.stream()
				.mapToLong(cartBook -> (long) cartBook.getBook().getPrice() * cartBook.getCount())
				.sum();
	}

	private int calculateTotalCount(List<CartBook> cartBooks) {
		return cartBooks.stream()
				.mapToInt(CartBook::getCount)
				.sum();
	}

}