package com.bbook.config;

import com.bbook.entity.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    log.info("OAuth2 로그인 성공");

    try {
      MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
      Member member = memberDetails.getMember();

      log.info("Logged in member: {}", member.getEmail());

      // 소셜 로그인 사용자이고 닉네임이 없는 경우
      if (member.isSocialMember() && member.getNickname() == null) {
        log.info("Redirecting to nickname setup page");
        response.sendRedirect("/members/social/nickname");
      } else {
        log.info("Redirecting to main page");
        response.sendRedirect("/");
      }
    } catch (Exception e) {
      log.error("Authentication success handling failed", e);
      response.sendRedirect("/");
    }
  }
}