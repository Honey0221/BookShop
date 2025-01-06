package com.bbook.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.bbook.dto.CartDetailDto;
import com.bbook.dto.CartItemDto;
import com.bbook.dto.CartOrderDto;
import com.bbook.dto.OrderDto;
import com.bbook.entity.Book;
import com.bbook.entity.Cart;
import com.bbook.entity.CartItem;
import com.bbook.entity.Member;
import com.bbook.repository.CartItemRepository;
import com.bbook.repository.CartRepository;
import com.bbook.repository.BookRepository;
import com.bbook.repository.MemberRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final OrderService orderService;

	public Long addCart(CartItemDto cartItemDto, String email) {
		// Item 객체 DB애서 추출
		Book book = bookRepository.findById(cartItemDto.getBookId())
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
		CartItem savedCartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(),
				book.getId());
		// 추출된 CartItem 객체가 있으면
		if (savedCartItem != null) {
			savedCartItem.addCount(cartItemDto.getCount()); // 있는 객체에 수량 증가
			return savedCartItem.getId();
			// 추출된 CartItem 객체가 없으면
		} else {
			// CartItem 객체를 생성하고 save를 통해 DB에 저장
			CartItem cartItem = CartItem.createCartItem(cart, book,
					cartItemDto.getCount());
			cartItemRepository.save(cartItem);
			return cartItem.getId();
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
		cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

		return cartDetailDtoList;
	}

	@Transactional(readOnly = true)
	public boolean validateCartItem(Long cartItemId, String email) {
		// email을 이용해서 Member 엔티티 객체 추출
		Member curMember = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		// cartItemId를 이용해서 cartItem 엔티티 객체 추출
		CartItem cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(EntityExistsException::new);
		// Cart -> Member 엔티티 객체를 추출
		Member savedMember = cartItem.getCart().getMember();
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
	 * @param cartItemId 장바구니 상품 ID
	 * @param count      변경할 수량
	 * @throws EntityExistsException 장바구니 상품을 찾을 수 없는 경우
	 */
	@Transactional
	public void updateCartItemCount(Long cartItemId, int count) {
		CartItem cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다."));
		cartItem.updateCount(count);
	}

	/**
	 * 장바구니에서 상품을 삭제하는 메소드
	 * 
	 * @param cartItemId 삭제할 장바구니 상품 ID
	 * @throws EntityExistsException 장바구니 상품을 찾을 수 없는 경우
	 */
	@Transactional
	public void deleteCartItem(Long cartItemId) {
		CartItem cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(EntityExistsException::new);
		cartItemRepository.delete(cartItem);
	}

	public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email, String impUid, String merchantUid) {
		List<OrderDto> orderDtoList = new ArrayList<>();

		for (CartOrderDto cartOrderDto : cartOrderDtoList) {
			CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
					.orElseThrow(EntityExistsException::new);
			OrderDto orderDto = new OrderDto();

			orderDto.setBookId(cartItem.getBook().getId());
			orderDto.setCount(cartItem.getCount());
			orderDto.setImpUid(impUid);
			orderDto.setMerchantUid(merchantUid);

			orderDtoList.add(orderDto);
		}

		Long orderId = orderService.orders(orderDtoList, email);

		// Cart애서 있던 Item 주문이 되니까 CartItem 모두 삭제
		for (CartOrderDto cartOrderDto : cartOrderDtoList) {
			CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
					.orElseThrow(EntityExistsException::new);
			cartItemRepository.delete(cartItem);
		}
		return orderId;
	}

	@Transactional(readOnly = true)
	public OrderDto createTempOrderInfo(List<CartOrderDto> cartOrderDtoList, String email) {
		Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
		List<CartItem> cartItems = new ArrayList<>();

		// 장바구니 상품 정보 조회
		for (CartOrderDto cartOrderDto : cartOrderDtoList) {
			CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
					.orElseThrow(EntityNotFoundException::new);
			cartItems.add(cartItem);
		}

		// 주문 정보 생성
		OrderDto orderDto = new OrderDto();
		Long originalPrice = calculateTotalPrice(cartItems); // 순수 상품 금액

		// 주문 정보 설정
		orderDto.setOrderName(createOrderName(cartItems));
		orderDto.setOriginalPrice(originalPrice); // 순수 상품 금액 설정
		orderDto.setTotalPrice(originalPrice); // 순수 상품 금액으로 설정 (배송비는 프론트에서 계산)
		orderDto.setCount(calculateTotalCount(cartItems));
		orderDto.setEmail(email);
		orderDto.setName(member.getNickname());

		// 첫 번째 상품의 대표 이미지 URL 설정
		if (!cartItems.isEmpty()) {
			Book book = cartItems.get(0).getBook();
			orderDto.setImageUrl(book.getImageUrl());
		}

		return orderDto;
	}

	private String createOrderName(List<CartItem> cartItems) {
		String firstItemName = cartItems.get(0).getBook().getTitle();
		if (cartItems.size() > 1) {
			return firstItemName + " 외 " + (cartItems.size() - 1) + "건";
		}
		return firstItemName;
	}

	private Long calculateTotalPrice(List<CartItem> cartItems) {
		return cartItems.stream()
				.mapToLong(item -> (long) item.getBook().getPrice() * item.getCount())
				.sum();
	}

	private int calculateTotalCount(List<CartItem> cartItems) {
		return cartItems.stream()
				.mapToInt(CartItem::getCount)
				.sum();
	}

}