package com.bbook.repository;

import com.bbook.entity.Coupon;
import com.bbook.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // 회원과 사용 여부가 false인 쿠폰 목록을 반환합니다.
    List<Coupon> findByMemberAndIsUsedFalse(Member member);

    // 회원과 사용 여부가 false인 쿠폰의 총 개수를 반환합니다.
    long countByMemberAndIsUsedFalse(Member member);

    // 회원과 사용 여부가 false인 첫 번째 쿠폰을 반환합니다.
    Optional<Coupon> findFirstByMemberAndIsUsedFalse(Member member);

    // 회원과 사용 여부가 true인 첫 번째 쿠폰을 반환합니다.
    Optional<Coupon> findFirstByMemberAndIsUsedTrue(Member member);

    boolean existsByMemberAndIsUsedFalseAndExpirationDateAfter(Member member, LocalDateTime date);
}