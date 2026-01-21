package com.onbok.book_hub.user.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 사용자 조회 전용 Service (Query)
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;

    /**
     * ID로 사용자 조회
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일로 사용자 조회 (없으면 예외 발생)
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일로 사용자 조회 (Optional 반환)
     */
    public User findByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 전체 사용자 목록 조회
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
