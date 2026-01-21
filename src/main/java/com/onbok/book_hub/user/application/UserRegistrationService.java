package com.onbok.book_hub.user.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.user.domain.model.LoginProvider;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import com.onbok.book_hub.user.dto.UserRegistrationRequestDto;
import com.onbok.book_hub.user.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 사용자 등록 관련 비즈니스 로직 (검증 + 생성)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 등록 검증 및 처리
     */
    public void registerUser(UserRegistrationRequestDto dto) {
        // 1. 이메일 중복 검증
        if (userQueryService.findByEmailOrNull(dto.email()) != null) {
            throw new ExpectedException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 길이 검증
        if (dto.pwd().length() < 4) {
            throw new ExpectedException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        // 3. 비밀번호 일치 여부 검증
        if (!dto.pwd().equals(dto.pwd2())) {
            throw new ExpectedException(ErrorCode.INVALID_PASSWORD);
        }

        // 사용자 생성 및 저장
        String hashedPwd = passwordEncoder.encode(dto.pwd());
        User user = User.builder()
                .pwd(hashedPwd)
                .uname(dto.uname())
                .email(dto.email())
                .profileUrl(dto.profileUrl())
                .loginProvider(LoginProvider.LOCAL)
                .build();

        log.info("회원가입 raw pwd = {}", dto.pwd());
        log.info("회원가입 encoded pwd = {}", hashedPwd);

        userRepository.save(user);
    }
}
