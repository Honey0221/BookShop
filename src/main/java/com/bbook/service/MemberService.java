package com.bbook.service;

import com.bbook.config.SecurityUtil;
import com.bbook.dto.MemberSignUpDto;
import com.bbook.entity.Member;
import com.bbook.repository.MemberRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil; // 현재 로그인한 사용자 정보 조회 유틸
    private final CouponService couponService;

    // 현재 로그인한 사용자의 ID 조회
    @Transactional
    public Optional<String> getCurrentMemberEmail() {
        return securityUtil.getCurrentUsername();  // 현재 로그인한 사용자의 이메일 조회
    }

    // 일반 회원가입
    @Transactional
    public Member signUp(MemberSignUpDto memberSignUpDto) {
        validateDuplicateEmail(memberSignUpDto.getEmail());
        validateDuplicateNickname(memberSignUpDto.getNickname());

        Member member = Member.createMember(memberSignUpDto, passwordEncoder);
        member = memberRepository.save(member);

        // 회원가입 시 쿠폰 발급
        couponService.createBasicCoupons(member);

        return member;
    }

    // 소셜 로그인 처리
    @Transactional
    public Member processSocialLogin(String email) {
        try {
            return memberRepository.findByEmail(email)
                    .map(member -> {
                        // 기존 회원인 경우
                        if (!member.isSocialMember()) {
                            log.error("일반 회원으로 가입된 이메일 소셜 로그인 시도: {}", email);
                            throw new IllegalStateException("일반 회원으로 가입된 이메일입니다. 일반 로그인을 이용해주세요.");
                        }
                        return member;
                    })
                    .orElseGet(() -> {
                        // 신규 회원인 경우 소셜 회원으로 가입
                        Member newMember = Member.createSocialMember(email);
                        log.info("새로운 소셜 회원 가입: {}", email);
                        Member savedMember = memberRepository.save(newMember);

                        // 소셜 로그인으로 가입한 신규 회원에게도 쿠폰 발급
                        couponService.createBasicCoupons(savedMember);

                        return savedMember;
                    });
        } catch (IllegalStateException e) {
            // 일반 회원으로 가입된 경우의 예외를 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생", e);
            throw new RuntimeException("소셜 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    // 닉네임 설정 (소셜 로그인 사용자용)
    @Transactional
    public void setNickname(Long memberId, String nickname) {
        validateDuplicateNickname(nickname);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (!member.isSocialMember()) {
            throw new IllegalStateException("소셜 로그인 회원만 이 기능을 사용할 수 있습니다.");
        }

        member.setInitialNickname(nickname);
    }

    // 회원 정보 수정
    @Transactional
    public void updateMemberInfo(Long memberId, String nickname, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 닉네임 변경 요청이 있는 경우
        if (nickname != null && !nickname.equals(member.getNickname())) {
            validateDuplicateNickname(nickname);
            member.setNickname(nickname);
        }

        // 비밀번호 변경 요청이 있는 경우 (일반 회원만)
        if (!member.isSocialMember() && newPassword != null) {
            if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            member.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    // 중복 검사 메소드들
    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }
    }

    // 회원 조회
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }

    // 소셜 로그인 사용자 중 닉네임 미설정 회원 조회
    public List<Member> findSocialMembersWithoutNickname() {
        return memberRepository.findSocialMembersWithoutNickname();
    }

    public boolean existsByEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }
    
    public Long getMemberIdByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow().getId();
    }

}
