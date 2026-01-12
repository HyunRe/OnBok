package com.onbok.book_hub.common.security.oauth2;

import com.onbok.book_hub.user.application.UserCommandService;
import com.onbok.book_hub.user.application.UserQueryService;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyOAuth2UserService extends DefaultOAuth2UserService {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String email, uname, profileUrl;
        String hashedPwd = bCryptPasswordEncoder.encode("Social Login");
        User user = null;

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("===getAttributes()===" + oAuth2User.getAttributes());
        String provider = userRequest.getClientRegistration().getRegistrationId();
        switch (provider) {
            case "github":
                Long id = oAuth2User.getAttribute("id");
                user = userQueryService.findById(id);
                if (user == null) {     // DB 내에 사용자가 없으면 register 수행
                    uname = oAuth2User.getAttribute("name");
                    email = oAuth2User.getAttribute("email");
                    profileUrl = oAuth2User.getAttribute("avatar_url");
                    user = User.builder()
                            .pwd(hashedPwd)
                            .uname(uname)
                            .email(email)
                            .regDate(LocalDate.now())
                            .role("ROLE_USER")
                            .provider(provider)
                            .profileUrl(profileUrl)
                            .build();
                    userCommandService.registerUser(user);
                    log.info("깃허브 계정을 통해 회원 가입이 되었습니다. " + user.getUname());
                }
                break;

            case "google":
                Long sub = oAuth2User.getAttribute("sub");
                user = userQueryService.findById(sub);
                if (user == null) {     // DB 내에 사용자가 없으면 register 수행
                    uname = oAuth2User.getAttribute("name");
                    uname = (uname == null) ? "google_user" : uname;
                    email = oAuth2User.getAttribute("email");
                    profileUrl = oAuth2User.getAttribute("picture");
                    user = User.builder()
                            .pwd(hashedPwd)
                            .uname(uname)
                            .email(email)
                            .regDate(LocalDate.now())
                            .role("ROLE_USER")
                            .provider(provider)
                            .profileUrl(profileUrl)
                            .build();
                    userCommandService.registerUser(user);
                    log.info("구글 계정을 통해 회원 가입이 되었습니다. " + user.getUname());
                }
                break;

            case "naver":
                Map response = oAuth2User.getAttribute("response");
                Long nid = (Long) Objects.requireNonNull(response).get("id");
                user = userQueryService.findById(nid);
                if (user == null) {     // DB 내에 사용자가 없으면 register 수행
                    uname = (String) response.get("nickname");
                    uname = (uname == null) ? "naver_user" : uname;
                    email = (String) response.get("email");
                    profileUrl = (String) response.get("profile_image");
                    user = User.builder()
                            .pwd(hashedPwd)
                            .uname(uname)
                            .email(email)
                            .regDate(LocalDate.now())
                            .role("ROLE_USER")
                            .provider(provider)
                            .profileUrl(profileUrl)
                            .build();
                    userCommandService.registerUser(user);
                    log.info("네이버 계정을 통해 회원 가입이 되었습니다. " + user.getUname());
                }
                break;

            case "kakao":
                Long kid = oAuth2User.getAttribute("id");
                user = userQueryService.findById(kid);
                if (user == null) {         // 내 DB에 없으면 가입을 시켜줌
                    Map properties = oAuth2User.getAttribute("properties");
                    Map account = oAuth2User.getAttribute("kakao_account");
                    uname = (String) Objects.requireNonNull(properties).get("nickname");
                    uname = (uname == null) ? "kakao_user" : uname;
                    email = (String) Objects.requireNonNull(account).get("email");
                    profileUrl = (String) properties.get("profile_image");
                    user = User.builder()
                            .pwd(hashedPwd)
                            .uname(uname)
                            .email(email)
                            .regDate(LocalDate.now())
                            .role("ROLE_USER")
                            .provider(provider)
                            .profileUrl(profileUrl)
                            .build();
                    userCommandService.registerUser(user);
                    log.info("카카오 계정을 통해 회원가입이 되었습니다. " + user.getUname());
                }
                break;

            case "facebook":
                Long fid = oAuth2User.getAttribute("id");    // Facebook ID
                user = userQueryService.findById(fid);
                if (user == null) {         // 내 DB에 없으면 가입을 시켜줌
                    uname = oAuth2User.getAttribute("name");
                    uname = (uname == null) ? "facebook_user" : uname;
                    email = oAuth2User.getAttribute("email");
                    user = User.builder()
                            .pwd(hashedPwd)
                            .uname(uname)
                            .email(email)
                            .regDate(LocalDate.now())
                            .role("ROLE_USER")
                            .provider(provider)
                            .build();
                    userCommandService.registerUser(user);
                    log.info("페이스북 계정을 통해 회원가입이 되었습니다. " + user.getUname());
                }
                break;
        }

        return new MyUserDetails(user, oAuth2User.getAttributes());
    }
}
