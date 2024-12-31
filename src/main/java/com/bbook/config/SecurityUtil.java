package com.bbook.config;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityUtil {

	// 현재 로그인한 사용자의 이메일(username) 조회
	public Optional<String> getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() ||
				authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}

		return Optional.ofNullable(authentication.getName());
	}
}