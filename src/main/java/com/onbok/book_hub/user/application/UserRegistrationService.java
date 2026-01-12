package com.onbok.book_hub.user.application;

import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.dto.UserRegistrationRequestDto;
import com.onbok.book_hub.user.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 사용자 등록 관련 비즈니스 로직 (검증 + 생성)
 */
@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    /**
     * 사용자 등록 검증 및 처리
     */
    public boolean registerUser(UserRegistrationRequestDto userRegistrationRequestDto) {
        // 검증: 사용자 존재 여부, 비밀번호 일치 여부, 비밀번호 길이
        if (userQueryService.findById(userRegistrationRequestDto.getId()) != null) {
            return false;
        }
        if (!userRegistrationRequestDto.getPwd().equals(userRegistrationRequestDto.getPwd2())) {
            return false;
        }
        if (userRegistrationRequestDto.getPwd().length() < 4) {
            return false;
        }

        // 사용자 생성
        String hashedPwd = BCrypt.hashpw(userRegistrationRequestDto.getPwd(), BCrypt.gensalt());
        User user = User.builder()
                .pwd(hashedPwd)
                .uname(userRegistrationRequestDto.getUname())
                .email(userRegistrationRequestDto.getEmail())
                .profileUrl(userRegistrationRequestDto.getProfileUrl())
                .regDate(LocalDate.now())
                .role("ROLE_USER")
                .provider("local")
                .build();

        userCommandService.registerUser(user);
        return true;
    }

    /**
     * 사용자 정보 수정 검증 및 처리
     */
    public void updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        User user = userQueryService.findById(userUpdateRequestDto.getId());
        if (user == null) {
            return;
        }

        // 비밀번호 변경이 있는 경우에만 검증 및 업데이트
        if (userUpdateRequestDto.getPwd() != null && !userUpdateRequestDto.getPwd().isEmpty()) {
            if (!userUpdateRequestDto.getPwd().equals(userUpdateRequestDto.getPwd2()) || userUpdateRequestDto.getPwd().length() < 4) {
                return;
            }
            String hashedPwd = BCrypt.hashpw(userUpdateRequestDto.getPwd(), BCrypt.gensalt());
            user.updatePassword(hashedPwd);
        }

        user.updateProfile(userUpdateRequestDto.getUname(), userUpdateRequestDto.getEmail(), userUpdateRequestDto.getProfileUrl(), userUpdateRequestDto.getRole(), userUpdateRequestDto.getProvider());

        userCommandService.updateUser(user);
    }
}
