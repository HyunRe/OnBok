package com.onbok.book_hub.common.security.oauth2;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.user.domain.model.LoginProvider;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuthUserCommandService oAuthUserCommandService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        User user = switch (provider) {
            case "github" -> {
                // 1. 이메일이 null일 경우를 대비해 'login'(사용자ID)을 가져옵니다.
                String email = oAuth2User.getAttribute("email");
                String login = oAuth2User.getAttribute("login"); // 깃허브 닉네임/아이디

                // 이메일이 없으면 깃허브ID@github.com 형태로라도 ID를 생성 (비즈니스 정책에 따라 선택)
                if (email == null) {
                    email = login + "@github.com";
                }

                OAuthUserInfo info = new OAuthUserInfo(
                        email, // 이제 null이 아님을 보장하거나 예외 처리를 커스텀하게 할 수 있습니다.
                        oAuth2User.getAttribute("name") != null ? oAuth2User.getAttribute("name") : login,
                        oAuth2User.getAttribute("avatar_url"),
                        LoginProvider.GITHUB
                );
                yield oAuthUserCommandService.findOrCreateOAuthUser(info);
            }
            case "google" -> {
                OAuthUserInfo info = new OAuthUserInfo(
                        oAuth2User.getAttribute("email"),
                        oAuth2User.getAttribute("name"),
                        oAuth2User.getAttribute("picture"),
                        LoginProvider.GOOGLE
                );
                yield oAuthUserCommandService.findOrCreateOAuthUser(info);
            }
            case "naver" -> {
                Map<String, Object> response = oAuth2User.getAttribute("response");
                OAuthUserInfo info = new OAuthUserInfo(
                        (String) Objects.requireNonNull(response).get("email"),
                        (String) response.get("nickname"),
                        (String) response.get("profile_image"),
                        LoginProvider.NAVER
                );
                yield oAuthUserCommandService.findOrCreateOAuthUser(info);
            }
            default -> throw new ExpectedException(ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        };

        return new MyUserDetails(user, oAuth2User.getAttributes());
    }
}
