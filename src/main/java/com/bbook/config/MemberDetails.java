package com.bbook.config;

import com.bbook.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class MemberDetails implements UserDetails, OAuth2User {
  private final Member member;
  private Map<String, Object> attributes;

  // 일반 로그인용 생성자
  public MemberDetails(Member member) {
    this.member = member;
  }

  // OAuth2 로그인용 생성자
  public MemberDetails(Member member, Map<String, Object> attributes) {
    this.member = member;
    this.attributes = attributes;
  }

  // OAuth2User 구현
  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return member.getEmail();
  }

  // UserDetails 구현
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority(member.getRole().toString()));
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public String getUsername() {
    return member.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}