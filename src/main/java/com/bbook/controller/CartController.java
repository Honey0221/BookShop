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

import com.bbook.constant.ActivityType;
import com.bbook.dto.CartDetailDto;
import com.bbook.dto.CartBookDto;
import com.bbook.dto.BookRecommendationDto;
import com.bbook.service.CartService;
import com.bbook.service.MemberActivityService;
import com.bbook.entity.Member;
import com.bbook.entity.Subscription;
import com.bbook.repository.MemberRepository;
import com.bbook.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;
	private final MemberActivityService memberActivityService;
	private final MemberRepository memberRepository;
	private final SubscriptionRepository subscriptionRepository;

	/**
	 * 장바구니에 상품을 추가하는 API 엔드포인트
	 * 
	 * @param cartBookDto   장바구니에 담을 상품 정보 (상품 ID, 수량 등)
	 * @param bindingResult 검증 결과
	 * @param principal     현재 로그인한 사용자 정보
	 * @return ResponseEntity 장바구니 아이템 ID 또는 에러 메시지
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/cart")
	public @ResponseBody ResponseEntity order(
			@RequestBody @Valid CartBookDto cartBookDto,
			BindingResult bindingResult, Principal principal) {
		// 입력값 검증 실패시 에러 메시지 반환
		System.out.println("cartBookDto: " + cartBookDto);
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			List<FieldError> fieldErrors = bindingResult.getFieldErrors();
			for (FieldError fieldError : fieldErrors) {
				sb.append(fieldError.getDefaultMessage());
			}
			return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
		}

		String email = principal.getName();
		Long cartBookId;
		System.out.println("cartBookDto: " + cartBookDto);
		try {
			// 장바구니에 상품 추가 후 생성된 장바구니 아이템 ID 반환
			cartBookId = cartService.addCart(cartBookDto, email);
			memberActivityService.saveActivity(email, cartBookDto.getBookId(), ActivityType.CART);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Long>(cartBookId, HttpStatus.OK);
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

		// 구독 상태 확인
		Member member = memberRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		Subscription subscription = subscriptionRepository.findByMemberId(member.getId())
				.orElse(null);
		boolean isSubscriber = subscription != null && subscription.isActive();

		// 맞춤 추천 도서 조회
		List<BookRecommendationDto> personalizedBooks = memberActivityService
				.getHybridRecommendations(principal.getName());

		// 뷰에 장바구니 아이템 목록과 구독 상태 전달
		model.addAttribute("cartBooks", cartDetailList);
		model.addAttribute("isSubscriber", isSubscriber);
		model.addAttribute("personalizedBooks", personalizedBooks);
		return "cart/cart";
	}

	@PatchMapping("/cartBook/{cartBookId}")
	@ResponseBody
	public ResponseEntity<String> updateCartBook(
			@PathVariable("cartBookId") Long cartBookId,
			@RequestBody CartBookDto cartBookDto,
			Principal principal) {

		if (cartBookDto.getCount() <= 0) {
			return new ResponseEntity<>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
		}

		try {
			cartService.updateCartBookCount(cartBookId, cartBookDto.getCount());
			return new ResponseEntity<>("수량을 변경했습니다.", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@SuppressWarnings("rawtypes")
	@DeleteMapping(value = "/cart/{cartBookId}")
	public @ResponseBody ResponseEntity deleteCartBook(
			@PathVariable("cartBookId") Long cartBookId,
			Principal principal) {
		if (!cartService.validateCartBook(cartBookId, principal.getName())) {
			return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
		}
		Long bookId = cartService.deleteCartBook(cartBookId);
		memberActivityService.cancelActivity(principal.getName(), bookId, ActivityType.CART);
		return new ResponseEntity<Long>(cartBookId, HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@DeleteMapping(value = "/cart/books")
	public @ResponseBody ResponseEntity deleteCartBooks(
			@RequestBody List<Long> cartBookIds,
			Principal principal) {

		for (Long cartBookId : cartBookIds) {
			if (!cartService.validateCartBook(cartBookId, principal.getName())) {
				return new ResponseEntity<String>("삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);
			}
		}

		try {
			for (Long cartBookId : cartBookIds) {
				Long bookId = cartService.deleteCartBook(cartBookId);
				memberActivityService.cancelActivity(principal.getName(), bookId, ActivityType.CART);
			}
			return new ResponseEntity<List<Long>>(cartBookIds, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
