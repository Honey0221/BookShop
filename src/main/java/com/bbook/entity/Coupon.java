package com.bbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 할인 금액
    @Column(name = "discount_value", nullable = false)
    private Integer discountAmount;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    // 사용 여부
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    // 최소 주문 금액
    @Column(name = "minimum_order_amount", nullable = false)
    private Integer minimumOrderAmount;

    // 템플릿 ID
    private Long templateId;

    // 만료일
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false)
    private CouponType couponType;

    public enum CouponType {
        SIGNUP, // 회원가입 시 자동 발급되는 쿠폰
        COUPON_ZONE // 쿠폰존에서 발급받는 쿠폰
    }

    public boolean isDownloaded() {
        return member != null;
    }

    public static Coupon createBasicCoupon(Member member) {
        Coupon coupon = new Coupon();
        coupon.setMember(member);
        coupon.setDiscountAmount(1000);
        coupon.setIsUsed(false);
        coupon.setMinimumOrderAmount(15000);
        coupon.setExpirationDate(LocalDateTime.now().plusDays(30));
        coupon.setAmount(1000);
        coupon.setTemplateId(1L);
        coupon.setCouponType(CouponType.SIGNUP);
        return coupon;
    }

    public static Coupon createCouponZoneCoupon(Member member) {
        Coupon coupon = createBasicCoupon(member);
        coupon.setCouponType(CouponType.COUPON_ZONE);
        return coupon;
    }

    public void setUsed(boolean used) {
        this.isUsed = used;
    }
}