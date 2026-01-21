package com.onbok.book_hub.user.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import com.onbok.book_hub.user.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 CRUD 관련 비즈니스 로직 (Command)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserCommandService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 수정 검증 및 처리
     */
    public void updateUser(User user, UserUpdateRequestDto userUpdateRequestDto) {
        try {
            log.info("사용자 정보 수정 시작 - userId: {}", user.getId());
            log.debug("수정 요청 데이터 - uname: {}, profileUrl: {}, pwd: {}",
                    userUpdateRequestDto.uname(),
                    userUpdateRequestDto.profileUrl(),
                    userUpdateRequestDto.pwd() != null ? "****" : "null");

            // DB에서 최신 User 엔티티를 조회하여 영속성 컨텍스트에서 관리되도록 함
            User managedUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음 - userId: {}", user.getId());
                        return new ExpectedException(ErrorCode.USER_NOT_FOUND);
                    });

            log.debug("DB에서 사용자 조회 완료 - userId: {}, uname: {}", managedUser.getId(), managedUser.getUname());

            // 비밀번호 변경이 있는 경우에만 검증 및 업데이트
            if (userUpdateRequestDto.pwd() != null && !userUpdateRequestDto.pwd().isEmpty()) {
                log.debug("비밀번호 변경 요청 감지");
                // 비밀번호 길이 검증
                if (userUpdateRequestDto.pwd().length() < 4) {
                    log.warn("비밀번호 길이 부족 - length: {}", userUpdateRequestDto.pwd().length());
                    throw new ExpectedException(ErrorCode.PASSWORD_TOO_SHORT);
                }
                // 비밀번호 일치 여부 검증
                if (!userUpdateRequestDto.pwd().equals(userUpdateRequestDto.pwd2())) {
                    log.warn("비밀번호 확인 불일치");
                    throw new ExpectedException(ErrorCode.INVALID_PASSWORD);
                }
                String hashedPwd = passwordEncoder.encode(userUpdateRequestDto.pwd());
                managedUser.updatePassword(hashedPwd);
                log.debug("비밀번호 변경 완료");
            }

            log.debug("프로필 정보 업데이트 중 - uname: {}, profileUrl: {}",
                    userUpdateRequestDto.uname(), userUpdateRequestDto.profileUrl());
            managedUser.updateProfile(userUpdateRequestDto.uname(), userUpdateRequestDto.profileUrl());

            log.info("사용자 정보 수정 완료 - userId: {}", managedUser.getId());
            // @Transactional이 있으므로 명시적인 save() 호출 없이도 변경사항이 자동으로 DB에 반영됨 (Dirty Checking)
        } catch (ExpectedException e) {
            log.error("사용자 정보 수정 실패 (ExpectedException) - userId: {}, error: {}",
                    user.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패 (Exception) - userId: {}, error: {}",
                    user.getId(), e.getMessage(), e);
            throw new RuntimeException("사용자 정보 수정 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
