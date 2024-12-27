package com.bbook.config;

import com.bbook.entity.Member;
import com.bbook.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final MemberService memberService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		try {
			log.info("OAuth2 로그인 시작");

			OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
			OAuth2User oAuth2User = delegate.loadUser(userRequest);

			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
					.getUserInfoEndpoint().getUserNameAttributeName();

			log.info("Provider: {}", registrationId);
			log.info("UserNameAttributeName: {}", userNameAttributeName);
			log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());

			OAuthAttributes attributes = OAuthAttributes.of(registrationId,
					userNameAttributeName,
					oAuth2User.getAttributes());

			log.info("Extracted email: {}", attributes.getEmail());

			Member member = memberService.processSocialLogin(
					attributes.getEmail());

			log.info("Member saved/updated: {}", member.getId());

			return new MemberDetails(member, attributes.getAttributes());

		} catch (Exception e) {
			log.error("OAuth2 로그인 실패", e);
			throw new OAuth2AuthenticationException(e.getMessage());
		}
	}
}
