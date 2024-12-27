package com.bbook.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    // 예외 정보 로깅
    log.error("Authentication failed: ", exception);

    String errorMessage;
    // OAuth2AuthenticationException에서 직접 메시지를 가져옴
    if (exception instanceof OAuth2AuthenticationException) {
      errorMessage = exception.getMessage();
      if (errorMessage == null || errorMessage.isEmpty()) {
        errorMessage = "이미 가입된 이메일입니다. 일반 로그인을 이용해주세요.";
      }
    } else {
      errorMessage = "로그인에 실패했습니다. 다시 시도해 주세요.";
    }

    log.error("Error message: {}", errorMessage);

    String redirectUrl = "/members/login?error=true&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    response.sendRedirect(redirectUrl);
  }
}