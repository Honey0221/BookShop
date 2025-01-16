package com.bbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    // 구독 시작 날짜
    private LocalDateTime startDate;
    // 구독 종료 날짜
    private LocalDateTime endDate;
    private Integer price;
    // 구독이 활성화되어 있는지 여부 기본값은 (true)
    @Column(name = "is_active")
    private boolean isActive = true;
    // 상점 UID
    private String merchantUid;
    // 아임포트 UID
    private String impUid;
    // 고객 UID
    private String customerUid;
    // 다음 결제 날짜 
    private LocalDateTime nextPaymentDate;

    public enum SubscriptionType {
        MONTHLY,
        YEARLY
    }

    public static Subscription createSubscription(Member member, SubscriptionType type) {
        Subscription subscription = new Subscription();
        subscription.setMember(member);
        subscription.setType(type);

        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        subscription.setNextPaymentDate(now.plusMinutes(1)); // 다음 결제일을 1분 후로 설정

        // 구독 기간과 가격 설정
        if (type == SubscriptionType.MONTHLY) {
            subscription.setEndDate(now.plusMonths(1));
            subscription.setPrice(9900);
        } else {
            subscription.setEndDate(now.plusYears(1));
            subscription.setPrice(99000);
        }

        subscription.setActive(true);
        return subscription;
    }
}