package com.bbook.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.dto.CartDetailDto;
import com.bbook.dto.CartItemDto;
import com.bbook.dto.CartOrderDto;
import com.bbook.dto.OrderDto;
import com.bbook.service.CartService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;

	/**
	 * 장바구니에 상품을 추가하는 API 엔드포인트
	 * 
	 * @param cartItemDto   장바구니에 담을 상품 정보 (상품 ID, 수량 등)
	 * @param bindingResult 검증 결과
	 * @param principal     현재 로그인한 사용자 정보
	 * @return ResponseEntity 장바구니 아이템 ID 또는 에러 메시지
	 */
	@PostMapping(value = "/cart")
	public @ResponseBody ResponseEntity order(
			@RequestBody @Valid CartItemDto cartItemDto,
			BindingResult bindingResult, Principal principal) {
		// 입력값 검증 실패시 에러 메시지 반환
		System.out.println("cartItemDto: " + cartItemDto);
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			List<FieldError> fieldErrors = bindingResult.getFieldErrors();
			for (FieldError fieldError : fieldErrors) {
				sb.append(fieldError.getDefaultMessage());
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
		}

		String email = principal.getName();
		Long cartItemId;
		System.out.println("cartItemDto: " + cartItemDto);
		try {
			// 장바구니에 상품 추가 후 생성된 장바구니 아이템 ID 반환
			cartItemId = cartService.addCart(cartItemDto, email);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
	}

	/**
	 * 장바구니 페이지를 보여주는 컨트롤러 메서드
	 * 
	 * @param principal 현재 로그인한 사용자 정보
	 * @param model     뷰에 전달할 데이터를 담는 Model 객체
	 * @return 장바구니 목록 페이지 뷰 이름
	 */
	@GetMapping(value = "/cart")
	public String orderHist(Principal principal, Model model) {
		// 현재 사용자의 장바구니 목록 조회
		List<CartDetailDto> cartDetailList = cartService.getCartList(
				principal.getName());
		// 뷰에 장바구니 아이템 목록 전달
		model.addAttribute("cartItems", cartDetailList);
		return "cart/cartList";
	}

	@PatchMapping("/cartItem/{cartItemId}")
	@ResponseBody
	public ResponseEntity<String> updateCartItem(
			@PathVariable("cartItemId") Long cartItemId,
			@RequestBody CartItemDto cartItemDto,
			Principal principal) {

		if (cartItemDto.getCount() <= 0) {
			return new ResponseEntity<>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
		}

		try {
			cartService.updateCartItemCount(cartItemId, cartItemDto.getCount());
			return new ResponseEntity<>("수량을 변경했습니다.", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping(value = "/cart/{cartItemId}")
	public @ResponseBody ResponseEntity deleteCartItem(
			@PathVariable("cartItemId") Long cartItemId,
			Principal principal) {
		if (!cartService.validateCartItem(cartItemId, principal.getName())) {
			return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
		}
		cartService.deleteCartItem(cartItemId);
		return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
	}

	@DeleteMapping(value = "/cart/items")
	public @ResponseBody ResponseEntity deleteCartItems(
			@RequestBody List<Long> cartItemIds,
			Principal principal) {

		for (Long cartItemId : cartItemIds) {
			if (!cartService.validateCartItem(cartItemId, principal.getName())) {
				return new ResponseEntity<String>("삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);
			}
		}

		try {
			for (Long cartItemId : cartItemIds) {
				cartService.deleteCartItem(cartItemId);
			}
			return new ResponseEntity<List<Long>>(cartItemIds, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/cart/orders")
	public @ResponseBody ResponseEntity<?> orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal,
			HttpSession session) {
		List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

		if (cartOrderDtoList == null || cartOrderDtoList.isEmpty()) {
			return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.BAD_REQUEST);
		}

		try {
			// 주문 정보를 세션에 저장 (결제 완료 후 주문 생성에 사용)
			session.setAttribute("cartOrderDtoList", cartOrderDtoList);
			session.setAttribute("orderEmail", principal.getName());

			// 결제 페이지에 표시할 정보만 생성
			OrderDto tempOrderDto = cartService.createTempOrderInfo(cartOrderDtoList, principal.getName());
			return new ResponseEntity<OrderDto>(tempOrderDto, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}
