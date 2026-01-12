package com.onbok.book_hub.user.application;

import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.dto.UserLoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 관련 비즈니스 로직 (Command)
 */
@Service
@RequiredArgsConstructor
public class UserAuthService {
    public static final int CORRECT_LOGIN = 0;
    public static final int WRONG_PASSWORD = 1;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserQueryService userQueryService;

    /**
     * 로그인 검증 (이메일과 패스워드로 인증)
     */
    public int login(UserLoginRequestDto userLoginRequestDto) {
        User user = userQueryService.findByEmail(userLoginRequestDto.getEmail());
        if (bCryptPasswordEncoder.matches(userLoginRequestDto.getPassword(), user.getPwd())) {
            return CORRECT_LOGIN;
        } else {
            return WRONG_PASSWORD;
        }
    }
}
