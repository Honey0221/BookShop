package com.bbook.repository;

import com.bbook.entity.Coupon;
import com.bbook.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // 회원과 사용 여부가 false인 쿠폰 목록을 반환합니다.
    List<Coupon> findByMemberAndIsUsedFalse(Member member);

    // 회원과 사용 여부가 false인 첫 번째 쿠폰을 반환합니다.
    Optional<Coupon> findFirstByMemberAndIsUsedFalse(Member member);

    // 회원과 사용 여부가 true인 첫 번째 쿠폰을 반환합니다.
    Optional<Coupon> findFirstByMemberAndIsUsedTrue(Member member);

    // 회원이 특정 템플릿의 쿠폰을 가지고 있는지 확인합니다
    boolean existsByMemberAndCouponType(Member member, Coupon.CouponType couponType);
}