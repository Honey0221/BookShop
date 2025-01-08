package com.bbook.controller;

import com.bbook.dto.OrderDto;
import com.bbook.dto.OrderHistDto;
import com.bbook.entity.Book;
import com.bbook.entity.CartItem;
import com.bbook.entity.Order;
import com.bbook.exception.IamportResponseException;
import com.bbook.repository.BookRepository;
import com.bbook.repository.CartItemRepository;
import com.bbook.service.OrderService;
import com.bbook.service.CartService;
import com.bbook.service.MemberActivityService;
import com.bbook.client.IamportClient;
import com.bbook.client.IamportResponse;
import com.bbook.constant.ActivityType;
import com.bbook.dto.CancelData;
import com.bbook.dto.CartOrderDto;
import com.bbook.dto.Payment;
import com.bbook.entity.Member;
import com.bbook.repository.MemberRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {
	private final OrderService orderService;
	private final IamportClient iamportClient;
	private final CartService cartService;
	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;
	private final MemberActivityService memberActivityService;
	private final CartItemRepository cartItemRepository;

	@GetMapping(value = { "/orders", "/orders/{page}" })
	public String orderHist(@PathVariable("page") Optional<Integer> page,
			Principal principal, Model model) {
		Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4);

		Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(
				principal.getName(), pageable);

		// 날짜 형식 확인을 위한 로그
		ordersHistDtoList.getContent().forEach(order -> {
			log.info("Order ID: {}, Date: {}", order.getOrderId(), order.getOrderDate());
		});

		model.addAttribute("orders", ordersHistDtoList);
		model.addAttribute("page", pageable.getPageNumber());
		model.addAttribute("maxPage", 5);
		return "order/orderHist";
	}

	@PostMapping("/order/{orderId}/cancel")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
		Map<String, Object> response = new HashMap<>();

		try {
			Order order = orderService.findById(orderId);
			long totalAmount = order.getOrderItems().stream()
					.mapToLong(item -> item.getOrderPrice() * item.getCount())
					.sum();

			try {
				CancelData cancelData = new CancelData(order.getImpUid(), true);
				IamportResponse<Payment> cancelResponse = iamportClient.cancelPayment(cancelData);

				if (cancelResponse.getCode() == 0) {
					orderService.cancelOrder(orderId); // DB 상태 업데이트
					response.put("success", true);
					response.put("message", "주문이 성공적으로 취소되었습니다.");
					return ResponseEntity.ok(response);
				} else {
					throw new IamportResponseException(cancelResponse.getMessage());
				}
			} catch (IamportResponseException e) {
				log.error("Payment cancellation failed - code: {}, message: {}",
						e.getCode(), e.getMessage());
				response.put("success", false);
				response.put("message", "Payment cancellation failed: " + e.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		} catch (Exception e) {
			log.error("Order cancellation failed", e);
			response.put("success", false);
			response.put("message", "주문 취소 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/order/payment")
	public String orderPayment(Model model, HttpSession session, Principal principal) {
		try {
			// 사용자의 포인트 정보 조회
			Member member = memberRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
			model.addAttribute("memberPoint", member.getPoint());

			// 직접 주문인 경우
			OrderDto orderDto = (OrderDto) session.getAttribute("orderDto");
			if (orderDto != null) {
				model.addAttribute("orderDto", orderDto);
				return "order/payment";
			}

			// 장바구니 주문인 경우
			@SuppressWarnings("unchecked")
			List<CartOrderDto> cartOrderDtoList = (List<CartOrderDto>) session.getAttribute("cartOrderDtoList");
			if (cartOrderDtoList != null) {
				orderDto = cartService.createTempOrderInfo(cartOrderDtoList, principal.getName());
				model.addAttribute("orderDto", orderDto);
				return "order/payment";
			}

			// 둘 다 없는 경우
			return "redirect:/cart";
		} catch (Exception e) {
			log.error("결제 페이지 로딩 중 오류 발생: {}", e.getMessage());
			return "redirect:/cart";
		}
	}

	@PostMapping("/order/payment")
	@ResponseBody
	public ResponseEntity<?> orderPayment(@RequestBody Map<String, Object> payload,
			Principal principal,
			HttpSession session) {
		try {
			String email = principal.getName();

			// 세션 초기화
			session.removeAttribute("orderDto");
			session.removeAttribute("cartOrderDtoList");
			session.removeAttribute("orderEmail");

			if (payload.containsKey("bookId")) {
				// 직접 주문 처리
				OrderDto orderDto = new OrderDto();
				Long bookId = Long.parseLong(payload.get("bookId").toString());
				orderDto.setBookId(bookId);
				orderDto.setCount(Integer.parseInt(payload.get("count").toString()));
				orderDto.setTotalPrice(Long.parseLong(payload.get("totalPrice").toString()));

				// Book 정보 가져오기
				Book book = bookRepository.findById(bookId)
						.orElseThrow(() -> new EntityNotFoundException("Book not found"));

				// 주문 정보 설정
				orderDto.setEmail(email);
				orderDto.setOrderName(book.getTitle());
				orderDto.setImageUrl(book.getImageUrl());
				orderDto.setTotalPrice((long) (book.getPrice() * orderDto.getCount()));
				orderDto.setMerchantUid("ORDER-" + System.currentTimeMillis());

				// 배송비 계산 및 적용
				orderDto.setOriginalPrice(orderDto.getTotalPrice());
				if (orderDto.getTotalPrice() < 15000) {
					orderDto.setTotalPrice(orderDto.getTotalPrice() + 3000);
					orderDto.setShippingFee(3000L);
				}

				// 주문 정보를 세션에 저장 (실제 주문 생성은 결제 완료 후에 수행)
				session.setAttribute("orderDto", orderDto);

			} else if (payload.containsKey("cartItems")) {
				// 장바구니 주문 처리
				@SuppressWarnings("unchecked")
				List<Map<String, String>> cartItems = (List<Map<String, String>>) payload.get("cartItems");
				List<CartOrderDto> cartOrderDtoList = cartItems.stream()
						.map(item -> {
							CartOrderDto dto = new CartOrderDto();
							dto.setCartItemId(Long.parseLong(item.get("cartItemId")));
							return dto;
						})
						.collect(Collectors.toList());

				session.setAttribute("cartOrderDtoList", cartOrderDtoList);
				session.setAttribute("orderEmail", email);
			} else {
				throw new IllegalArgumentException("Invalid order request");
			}

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("주문 처리 중 오류 발생: {}", e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	/**
	 * 결제 검증을 처리하는 API 엔드포인트
	 * 
	 * 클라이언트로부터 결제 검증 요청을 받아 처리합니다:
	 * 1. 결제 금액이 실제 주문 금액과 일치하는지 검증
	 * 2. 검증 성공시 주문에 imp_uid(결제 고유번호) 저장
	 * 3. 검증 결과를 JSON 응답으로 반환
	 *
	 * @param request 결제 검증에 필요한 정보를 담은 DTO (imp_uid, merchant_uid, amount, status)
	 * @return 검증 결과를 담은 ResponseEntity
	 *         - 성공: {success: true}
	 *         - 실패: {success: false, message: 실패사유}
	 */
	@PostMapping("/orders/verify")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody PaymentVerificationRequest request,
			HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		try {
			log.info("결제 검증 시작 - impUid: {}, merchantUid: {}, amount: {}",
					request.getImpUid(), request.getMerchantUid(), request.getAmount());

			// 포인트 사용 정보 가져오기
			Integer usedPoints = (Integer) session.getAttribute("usedPoints");
			if (usedPoints == null) {
				usedPoints = 0;
			}

			boolean isValidPayment = orderService.verifyPayment(
					request.getImpUid(),
					request.getMerchantUid(),
					request.getAmount());

			if (isValidPayment) {
				OrderDto orderDto = (OrderDto) session.getAttribute("orderDto");
				log.info("세션에서 주문 정보 조회 - orderDto: {}", orderDto != null ? "존재" : "없음");

				if (orderDto != null) {
					// 단일 상품 주문 처리
					log.info("단일 상품 주문 처리 시작 - email: {}, bookId: {}, count: {}",
							orderDto.getEmail(), orderDto.getBookId(), orderDto.getCount());

					// 포인트 차감
					if (usedPoints > 0) {
						Member member = memberRepository.findByEmail(orderDto.getEmail())
								.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
						member.setPoint(member.getPoint() - usedPoints);
						memberRepository.save(member);

						// 주문 금액에서 포인트 차감
						orderDto.setTotalPrice(orderDto.getTotalPrice() - usedPoints);
					}

					Long orderId = orderService.order(orderDto, orderDto.getEmail());
					log.info("주문 생성 완료 - orderId: {}", orderId);

					Order order = orderService.findById(orderId);
					order.setImpUid(request.getImpUid());
					orderService.saveOrder(order);

					// 포인트 적립 (결제 금액의 5%)
					Member member = memberRepository.findByEmail(orderDto.getEmail())
							.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
					long earnedPoints = Math.round(orderDto.getTotalPrice() * 0.05);
					member.setPoint(member.getPoint() + earnedPoints);
					memberRepository.save(member);
					log.info("포인트 적립 완료 - 적립 포인트: {}", earnedPoints);

					memberActivityService.saveActivity(orderDto.getEmail(), orderDto.getBookId(), ActivityType.PURCHASE);
					// 세션에서 포인트 정보 제거
					session.removeAttribute("usedPoints");

					response.put("success", true);
					response.put("orderId", orderId);
					return ResponseEntity.ok(response);
				} else {
					// 장바구니 주문 처리
					@SuppressWarnings("unchecked")
					List<CartOrderDto> cartOrderDtoList = (List<CartOrderDto>) session.getAttribute("cartOrderDtoList");
					String orderEmail = (String) session.getAttribute("orderEmail");

					if (cartOrderDtoList != null && orderEmail != null) {
						log.info("장바구니 주문 처리 시작 - email: {}, 상품 수: {}",
								orderEmail, cartOrderDtoList.size());

						// 포인트 차감
						if (usedPoints > 0) {
							Member member = memberRepository.findByEmail(orderEmail)
									.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
							member.setPoint(member.getPoint() - usedPoints);
							memberRepository.save(member);
						}

						Long orderId = cartService.orderCartItem(cartOrderDtoList, orderEmail,
								request.getImpUid(), request.getMerchantUid());
						log.info("장바구니 주문 생성 완료 - orderId: {}", orderId);

						for (CartOrderDto dto : cartOrderDtoList) {
							CartItem cartItem = cartItemRepository.findById(dto.getCartItemId())
									.orElseThrow(EntityExistsException::new);
							memberActivityService.saveActivity(orderEmail, cartItem.getBook().getId(), ActivityType.PURCHASE);
						}

						cartService.deleteCartItems(cartOrderDtoList);
						// 포인트 적립 (결제 금액의 5%)
						Order order = orderService.findById(orderId);
						Member member = memberRepository.findByEmail(orderEmail)
								.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
						long earnedPoints = Math.round(order.getTotalPrice() * 0.05);
						member.setPoint(member.getPoint() + earnedPoints);
						memberRepository.save(member);
						log.info("포인트 적립 완료 - 적립 포인트: {}", earnedPoints);

						// 세션에서 포인트 정보 제거
						session.removeAttribute("usedPoints");

						response.put("success", true);
						response.put("orderId", orderId);
						return ResponseEntity.ok(response);
					}
				}

				response.put("success", false);
				response.put("message", "주문 정보를 찾을 수 없습니다.");
				return ResponseEntity.badRequest().body(response);
			} else {
				response.put("success", false);
				response.put("message", "결제 금액이 일치하지 않습니다.");
				return ResponseEntity.badRequest().body(response);
			}
		} catch (Exception e) {
			log.error("결제 검증/주문 생성 중 오류 발생: {}", e.getMessage(), e);
			response.put("success", false);
			response.put("message", "결제 검증 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 결제 검증 요을 위한 DTO
	@Getter
	@Setter
	static class PaymentVerificationRequest {
		private String impUid;
		private String merchantUid;
		private Long amount;
		private String status;
	}

	@GetMapping("/order/success/{orderId}")
	public String orderSuccess(@PathVariable Long orderId, Model model) {
		try {
			Order order = orderService.findById(orderId);
			OrderDto orderDto = OrderDto.of(order);

			// 가격 정보 설정
			orderDto.setOriginalPrice(order.getOriginalPrice());
			orderDto.setTotalPrice(order.getTotalPrice());

			model.addAttribute("order", orderDto);
			return "order/success";
		} catch (EntityNotFoundException e) {
			return "redirect:/";
		}
	}

	@GetMapping("/order/complete/{orderId}")
	public String orderComplete(@PathVariable Long orderId,
			@RequestParam(required = false) String imp_uid,
			@RequestParam(required = false) String merchant_uid,
			@RequestParam(required = false) String paid_amount,
			Model model) {
		try {
			// 모바일 결제 검증
			if (imp_uid != null && merchant_uid != null && paid_amount != null) {
				orderService.verifyPayment(imp_uid, merchant_uid, Long.valueOf(paid_amount));
			}

			// 주문 정보 조회
			OrderDto orderDto = orderService.getOrderDetails(orderId);
			model.addAttribute("order", orderDto);
			return "order/success";
		} catch (Exception e) {
			return "redirect:/";
		}
	}

	@PostMapping("/order/complete")
	@ResponseBody
	public ResponseEntity<Long> completeOrder(@RequestBody Map<String, String> payload,
			HttpSession session,
			Principal principal) {
		try {
			log.info("주문 완료 처리 시작 - 사용자: {}", principal.getName());

			@SuppressWarnings("unchecked")
			List<CartOrderDto> cartOrderDtoList = (List<CartOrderDto>) session.getAttribute("cartOrderDtoList");
			if (cartOrderDtoList == null) {
				log.error("장바구니 주문 정보 없음");
				throw new IllegalStateException("주문 정보를 찾을 수 없습니다.");
			}
			log.info("장바구니 상품 수: {}", cartOrderDtoList.size());

			// imp_uid와 merchant_uid 확인
			String impUid = payload.get("impUid");
			String merchantUid = payload.get("merchantUid");
			if (impUid == null || merchantUid == null) {
				log.error("결제 정보 누락 - impUid: {}, merchantUid: {}", impUid, merchantUid);
				throw new IllegalArgumentException("결제 정보가 누락되었습니다.");
			}
			log.info("결제 정보 확인 - impUid: {}, merchantUid: {}", impUid, merchantUid);

			// 실제 주문 생성
			Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName(), impUid, merchantUid);
			log.info("주문 생성 완료 - 주문번호: {}", orderId);

			// 세션에서 주문 정보 제거
			session.removeAttribute("cartOrderDtoList");
			session.removeAttribute("orderEmail");
			log.info("세션에서 주문 정보 제거 완료");

			return new ResponseEntity<>(orderId, HttpStatus.OK);
		} catch (Exception e) {
			log.error("주문 생성 중 오류 발생: {}", e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/order/updateQuantity")
	@ResponseBody
	public ResponseEntity<String> updateQuantity(
			@RequestBody Map<String, Object> payload,
			HttpSession session) {
		try {
			@SuppressWarnings("unchecked")
			List<CartOrderDto> cartOrderDtoList = (List<CartOrderDto>) session.getAttribute("cartOrderDtoList");

			if (cartOrderDtoList != null) {
				Long itemId = Long.parseLong(payload.get("itemId").toString());
				int newCount = Integer.parseInt(payload.get("count").toString());

				// 해당 상품의 수량 업데이트
				for (CartOrderDto dto : cartOrderDtoList) {
					if (dto.getCartItemId().equals(itemId)) {
						dto.setCount(newCount);
						break;
					}
				}

				// 업데이트된 리스트를 세션에 저장
				session.setAttribute("cartOrderDtoList", cartOrderDtoList);

				return ResponseEntity.ok("수량이 업데이트되었습니다.");
			}

			return ResponseEntity.badRequest().body("장바구니 정보를 찾을 수 없습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("수량 업데이트 중 오류가 발생했습니다.");
		}
	}

	@PostMapping("/order/apply-points")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> applyPoints(@RequestBody Map<String, Integer> request,
			HttpSession session, Principal principal) {
		Map<String, Object> response = new HashMap<>();

		try {
			int points = request.get("points");

			// 사용자의 보유 포인트 확인
			Member member = memberRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

			if (points < 0) {
				response.put("success", false);
				response.put("message", "포인트는 0 이상이어야 합니다.");
				return ResponseEntity.badRequest().body(response);
			}

			if (points > member.getPoint()) {
				response.put("success", false);
				response.put("message", "보유 포인트를 초과하여 사용할 수 없습니다.");
				return ResponseEntity.badRequest().body(response);
			}

			if (points % 100 != 0) {
				response.put("success", false);
				response.put("message", "포인트는 100P 단위로 사용 가능합니다.");
				return ResponseEntity.badRequest().body(response);
			}

			// 세션에 사용할 포인트 저장
			session.setAttribute("usedPoints", points);

			response.put("success", true);
			response.put("message", "포인트가 적용되었습니다.");
			response.put("appliedPoints", points);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "포인트 적용 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 구매 여부 검사
	@GetMapping("/orders/check/{bookId}")
	public ResponseEntity<Map<String, Boolean>> checkPurchased(
			@PathVariable Long bookId, Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String email = principal.getName();
		Long memberId = memberRepository.findByEmail(email).get().getId();

		boolean purchased = orderService.hasUserPurchasedBook(memberId, bookId);

		Map<String, Boolean> response = new HashMap<>();
		response.put("purchased", purchased);

		return ResponseEntity.ok(response);
	}
}
