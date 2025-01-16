package com.bbook.config;

import com.bbook.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;
	private final CustomOauth2UserService customOauth2UserService;
	private final CustomAuthenticationFailureHandler failureHandler;
	private final CustomAuthenticationSuccessHandler successHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico",
						"/error")
				.permitAll()
				.requestMatchers("/", "/members/**", "/item/**", "/images/**")
				.permitAll()
				.requestMatchers("/emailCheck")
				.permitAll()
				.requestMatchers("/api/**")
				.permitAll()
				.requestMatchers("/books/**")
				.permitAll()
				.requestMatchers("/book-list/**")
				.permitAll()
				.requestMatchers("/search")
				.permitAll()
				.requestMatchers("/wish/**")
				.permitAll()
				.requestMatchers("/cart/**")
				.permitAll()
				.requestMatchers("/reviews", "/reviews/**")
				.permitAll()
				.requestMatchers("/bookshop/review/**")
				.permitAll()
				.requestMatchers("/admin/**")
				.hasRole("ADMIN")
				.requestMatchers("/chat/**")
				.permitAll()
				.requestMatchers("/ws-chat/**")
				.permitAll()
				.requestMatchers("/faq", "/faq/**")
				.permitAll()
				.anyRequest()
				.authenticated()).formLogin(formLogin -> formLogin
						.loginPage("/members/login")
						.defaultSuccessUrl("/")
						.usernameParameter("email")
						.failureUrl("/members/login/error"))
				.logout(logout -> logout
						.logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
						.logoutSuccessUrl("/"))
				.oauth2Login(oauth2 -> oauth2
						.defaultSuccessUrl("/")
						.successHandler(successHandler)
						.failureHandler(failureHandler)
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOauth2UserService)))
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/item/**")
						.ignoringRequestMatchers("/items/**")
						.ignoringRequestMatchers("/reviews/**")
						.ignoringRequestMatchers("/api/**")
						.ignoringRequestMatchers("/cart/**")
						.ignoringRequestMatchers("/wish/**")
						.ignoringRequestMatchers("/order/payment/**")
						.ignoringRequestMatchers("/admin/**")
						.ignoringRequestMatchers("/ws-chat/**")
						.ignoringRequestMatchers("/chat/**"));

		http.exceptionHandling(exception -> exception
				.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));

		return http.build();
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailsService)
				.passwordEncoder(passwordEncoder());
	}
}
