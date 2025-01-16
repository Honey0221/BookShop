package com.bbook.repository;

import com.bbook.entity.Member;
import com.bbook.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("select count(s) > 0 from Subscription s where s.member = :member and s.endDate > :date")
    boolean existsByMemberAndEndDateAfter(@Param("member") Member member, @Param("date") LocalDateTime date);

    Optional<Subscription> findByMemberId(Long memberId);

    List<Subscription> findByIsActiveTrue();
}