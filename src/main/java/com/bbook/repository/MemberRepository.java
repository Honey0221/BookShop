package com.bbook.repository;

import com.bbook.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT m FROM Member m WHERE m.isSocialMember = true AND m.nickname IS NULL")
    List<Member> findSocialMembersWithoutNickname();

    Page<Member> findByEmailContaining(String email, Pageable pageable);

    Page<Member> findByNicknameContaining(String nickname, Pageable pageable);

}
