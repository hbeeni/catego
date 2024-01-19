package com.been.catego.config.oauth;

import com.been.catego.domain.User;
import com.been.catego.dto.PrincipalDetails;
import com.been.catego.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String loginId = (String) attributes.get("email");
        String providerId = (String) attributes.get("sub");
        String nickname = (String) attributes.get("name");

        User user = userRepository.findByLoginId(loginId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .loginId(loginId)
                        .providerId(providerId)
                        .nickname(nickname)
                        .build()));

        return PrincipalDetails.from(user, oAuth2User.getAttributes());
    }
}
