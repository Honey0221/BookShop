package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.Role;
import com.bbook.dto.MemberSignUpDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.crypto.password.PasswordEncoder;

// @EntityListeners(AuditingEntityListener.class) 
@Entity // 나 엔티티야
@Table(name = "members") // 테이블 명
@Getter
@Setter
@ToString
public class Member extends BaseEntity {
    // 기본키 컬럼명 = member_id AI-> 데이터 저장시 1씩 증가
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_social_member")
    private boolean isSocialMember = false;

    @Column(name = "point", columnDefinition = "bigint default 0")
    private Long point = 0L;

    @CreatedDate // 생성시 자동 저장
    @Column(updatable = false)
    private LocalDateTime createAt; // 등록일

    @Column
    private LocalDateTime subscriptionExpiryDate; 

    public LocalDateTime getSubscriptionExpiryDate() {
        return subscriptionExpiryDate;
    }

    // 일반 회원가입용 생성 메소드
    public static Member createMember(MemberSignUpDto signUpDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setEmail(signUpDto.getEmail());
        member.setNickname(signUpDto.getNickname());
        member.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        member.setRole(Role.ADMIN);
        member.setSocialMember(false);
        return member;
    }

    // 소셜 로그인 회원용 생성 메소드
    public static Member createSocialMember(String email) {
        Member member = new Member();
        member.setEmail(email);
        member.setRole(Role.ADMIN);
        member.setSocialMember(true);
        return member;
    }

    // 닉네임 설정 메소드
    public void setInitialNickname(String nickname) {
        if (this.nickname != null) {
            throw new IllegalStateException("이미 닉네임이 설정되어 있습니다.");
        }
        this.nickname = nickname;
    }

    // 포인트 추가 메소드
    public void addPoint(int point) {
        this.point += point;
        // 포인트가 음수가 되지 않도록 보장
        if (this.point < 0) {
            this.point = 0L;
        }
    }

    // 포인트 설정 메소드
    public void setPoint(long point) {
        this.point = Math.max(0, point); // 음수가 되지 않도록 보장
    }
}
