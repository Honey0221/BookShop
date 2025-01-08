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
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "discount_value", nullable = false)
    private Integer discountAmount;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    @Column(name = "minimum_order_amount", nullable = false)
    private Integer minimumOrderAmount;

    private Long templateId;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

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
        return coupon;
    }
}