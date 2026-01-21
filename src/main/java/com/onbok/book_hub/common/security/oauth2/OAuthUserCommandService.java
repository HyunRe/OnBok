package com.onbok.book_hub.common.security.oauth2;

import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthUserCommandService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findOrCreateOAuthUser(OAuthUserInfo oAuthUserInfo) {
        return userRepository.findByEmail(oAuthUserInfo.getEmail())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(oAuthUserInfo.getEmail())
                                .uname(oAuthUserInfo.getUname())
                                .profileUrl(oAuthUserInfo.getProfileUrl())
                                .loginProvider(oAuthUserInfo.getProvider())
                                .pwd(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .build()
                ));
    }
}

