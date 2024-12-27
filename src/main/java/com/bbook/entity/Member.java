package com.bbook.entity;

import java.time.LocalDateTime;

import com.bbook.constant.Role;
import com.bbook.dto.MemberSignUpDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@EntityListeners(AuditingEntityListener.class) 
@Entity // 나 엔티티야
@Table(name = "member") // 테이블 명
@Getter
@Setter
@ToString
public class Member {
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
    private Role role = Role.USER;

    @Column(name = "is_social_member")
    private boolean isSocialMember = false;

    @CreatedDate // 생성시 자동 저장
    @Column(updatable = false)
    private LocalDateTime createAt; // 등록일

    // 일반 회원가입용 생성 메소드
    public static Member createMember(MemberSignUpDto signUpDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setEmail(signUpDto.getEmail());
        member.setNickname(signUpDto.getNickname());
        member.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        member.setRole(Role.USER);
        member.setSocialMember(false);
        return member;
    }

    // 소셜 로그인 회원용 생성 메소드
    public static Member createSocialMember(String email) {
        Member member = new Member();
        member.setEmail(email);
        member.setRole(Role.USER);
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
}
