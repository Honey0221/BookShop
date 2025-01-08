package com.bbook.service;

import com.bbook.entity.Coupon;
import com.bbook.entity.Member;
import com.bbook.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 쿠폰 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정 (성능 최적화)
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * 신규 회원에게 기본 쿠폰을 발급합니다.
     * 최대 10개까지 미사용 쿠폰을 보유할 수 있으며, 부족한 만큼 추가 발급합니다.
     *
     * @param member 쿠폰을 발급받을 회원
     */
    @Transactional
    public void createBasicCoupons(Member member) {
        // 현재 보유한 미사용 쿠폰 수 조회
        long unusedCouponCount = couponRepository.countByMemberAndIsUsedFalse(member);
        // 추가로 발급할 쿠폰 수 계산 (최대 10개)
        int remainingCoupons = 10 - (int) unusedCouponCount;

        // 부족한 만큼 기본 쿠폰 생성 및 저장
        for (int i = 0; i < remainingCoupons; i++) {
            couponRepository.save(Coupon.createBasicCoupon(member));
        }
    }

    /**
     * 회원이 사용 가능한 쿠폰 목록을 조회합니다.
     *
     * @param member 조회할 회원
     * @return 사용 가능한 쿠폰 목록
     */
    public List<Coupon> getAvailableCoupons(Member member) {
        return couponRepository.findByMemberAndIsUsedFalse(member);
    }

    /**
     * 주문에 쿠폰을 적용하고 할인 금액을 반환합니다.
     * 
     * 쿠폰 적용 조건:
     * 1. 주문 금액이 15,000원 이상
     * 2. 사용 가능한 쿠폰이 있어야 함
     * 3. 주문 금액이 쿠폰의 최소 주문 금액 이상
     *
     * @param member      쿠폰을 사용할 회원
     * @param orderAmount 주문 금액
     * @return 할인 금액 (조건 미충족시 0 반환)
     */
    @Transactional
    public Integer applyCoupon(Member member, Integer orderAmount) {
        // 최소 주문 금액(15,000원) 체크
        if (orderAmount < 15000) {
            return 0;
        }

        // 사용 가능한 쿠폰 조회
        Optional<Coupon> unusedCoupon = couponRepository.findFirstByMemberAndIsUsedFalse(member);
        if (unusedCoupon.isEmpty()) {
            return 0;
        }

        Coupon coupon = unusedCoupon.get();
        // 쿠폰별 최소 주문 금액 체크
        if (orderAmount < coupon.getMinimumOrderAmount()) {
            return 0;
        }

        // 쿠폰 사용 처리
        coupon.setIsUsed(true);
        return coupon.getDiscountAmount();
    }

    /**
     * 주문 취소 시 사용된 쿠폰을 복원합니다.
     *
     * @param member 쿠폰을 복원할 회원
     */
    @Transactional
    public void restoreCoupon(Member member) {
        // 가장 최근에 사용된 쿠폰을 찾아서 복원
        Optional<Coupon> usedCoupon = couponRepository.findFirstByMemberAndIsUsedTrue(member);
        usedCoupon.ifPresent(coupon -> coupon.setIsUsed(false));
    }

    /**
     * 쿠폰을 다운로드합니다.
     *
     * @param member 쿠폰을 다운로드할 회원
     */
    @Transactional
    public void downloadCoupon(Member member) {
        // 현재 보유한 미사용 쿠폰 수 조회
        long unusedCouponCount = couponRepository.countByMemberAndIsUsedFalse(member);

        // 최대 보유 가능한 쿠폰 수 체크 (10개)
        if (unusedCouponCount >= 10) {
            throw new IllegalStateException("이미 최대 개수의 쿠폰을 보유하고 있습니다.");
        }

        Coupon coupon = Coupon.createBasicCoupon(member);
        couponRepository.save(coupon);
    }

    public boolean hasDownloadedCoupon(Member member) {
        // 현재 날짜 기준으로 사용 가능한 쿠폰이 있는지 확인
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.existsByMemberAndIsUsedFalseAndExpirationDateAfter(member, now);
    }
}