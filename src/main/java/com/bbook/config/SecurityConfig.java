package com.bbook.config;

import com.bbook.entity.Member;
import com.bbook.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
import lombok.RequiredArgsConstructor;

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
                                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                                .requestMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                                .requestMatchers("/emailCheck").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()).formLogin(formLogin -> formLogin
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
                                                                .userService(customOauth2UserService)));

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
