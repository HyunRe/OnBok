package com.onbok.book_hub.oauth2;

import com.onbok.book_hub.common.security.oauth2.OAuthUserCommandService;
import com.onbok.book_hub.common.security.oauth2.OAuthUserInfo;
import com.onbok.book_hub.user.domain.model.LoginProvider;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OAuth2 통합 테스트 - 소셜 로그인")
@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    OAuthUserCommandService oAuthUserCommandService;

    @Test
    @DisplayName("Google OAuth2 - 신규 회원 자동 생성")
    void googleOAuth2_newUser_created() {
        // given
        String email = "newuser@gmail.com";
        String name = "New User";
        String picture = "https://example.com/profile.jpg";

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("picture", picture);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo(email, name, picture, LoginProvider.GOOGLE);

        // when
        User user = oAuthUserCommandService.findOrCreateOAuthUser(oAuthUserInfo);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUname()).isEqualTo(name);
        assertThat(user.getProfileUrl()).isEqualTo(picture);
        assertThat(user.getLoginProvider()).isEqualTo(LoginProvider.GOOGLE);

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Google OAuth2 - 기존 회원 계정 매핑")
    void googleOAuth2_existingUser_mapped() {
        // given
        String email = "existing@gmail.com";
        User existingUser = User.builder()
                .email(email)
                .uname("Existing User")
                .loginProvider(LoginProvider.GOOGLE)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        com.onbok.book_hub.common.security.oauth2.OAuthUserInfo oAuthUserInfo =
                new com.onbok.book_hub.common.security.oauth2.OAuthUserInfo(
                        email, "Updated Name", "https://example.com/new.jpg", LoginProvider.GOOGLE
                );

        // when
        User user = oAuthUserCommandService.findOrCreateOAuthUser(oAuthUserInfo);

        // then
        assertThat(user).isEqualTo(existingUser);
        assertThat(user.getEmail()).isEqualTo(email);

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class)); // 새로 저장하지 않음
    }

    @Test
    @DisplayName("GitHub OAuth2 - 이메일 없을 경우 login@github.com 형태로 생성")
    void githubOAuth2_noEmail_generatesFallback() {
        // given
        String login = "octocat";
        String fallbackEmail = login + "@github.com";

        when(userRepository.findByEmail(fallbackEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .email(user.getEmail())
                    .uname(user.getUname())
                    .loginProvider(LoginProvider.GITHUB)
                    .build();
        });

        com.onbok.book_hub.common.security.oauth2.OAuthUserInfo oAuthUserInfo =
                new com.onbok.book_hub.common.security.oauth2.OAuthUserInfo(
                        fallbackEmail, login, "https://avatars.githubusercontent.com/u/1?v=4", LoginProvider.GITHUB
                );

        // when
        User user = oAuthUserCommandService.findOrCreateOAuthUser(oAuthUserInfo);

        // then
        assertThat(user.getEmail()).isEqualTo(fallbackEmail);
        assertThat(user.getLoginProvider()).isEqualTo(LoginProvider.GITHUB);
    }

    @Test
    @DisplayName("Naver OAuth2 - 신규 회원 자동 생성")
    void naverOAuth2_newUser_created() {
        // given
        String email = "naver_user@naver.com";
        String nickname = "네이버유저";
        String profileImage = "https://example.com/naver_profile.jpg";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .email(user.getEmail())
                    .uname(user.getUname())
                    .profileUrl(user.getProfileUrl())
                    .loginProvider(LoginProvider.NAVER)
                    .build();
        });

        com.onbok.book_hub.common.security.oauth2.OAuthUserInfo oAuthUserInfo =
                new com.onbok.book_hub.common.security.oauth2.OAuthUserInfo(
                        email, nickname, profileImage, LoginProvider.NAVER
                );

        // when
        User user = oAuthUserCommandService.findOrCreateOAuthUser(oAuthUserInfo);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUname()).isEqualTo(nickname);
        assertThat(user.getLoginProvider()).isEqualTo(LoginProvider.NAVER);
    }

    @Test
    @DisplayName("동일 이메일로 여러 Provider 로그인 시 기존 계정 사용")
    void sameEmail_differentProviders_usesExistingAccount() {
        // given
        String email = "user@example.com";
        User existingUser = User.builder()
                .email(email)
                .uname("Original User")
                .loginProvider(LoginProvider.GOOGLE)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // GitHub로 같은 이메일 로그인 시도
        com.onbok.book_hub.common.security.oauth2.OAuthUserInfo githubInfo =
                new com.onbok.book_hub.common.security.oauth2.OAuthUserInfo(
                        email, "GitHub User", "https://example.com/github.jpg", LoginProvider.GITHUB
                );

        // when
        User user = oAuthUserCommandService.findOrCreateOAuthUser(githubInfo);

        // then
        assertThat(user).isEqualTo(existingUser);
        assertThat(user.getLoginProvider()).isEqualTo(LoginProvider.GOOGLE); // 기존 Provider 유지

        verify(userRepository, never()).save(any(User.class));
    }
}
