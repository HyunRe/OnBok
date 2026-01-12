package com.onbok.book_hub.user.application;

import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 CRUD 관련 비즈니스 로직 (Command)
 */
@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserQueryService userQueryService;

    /**
     * 사용자 등록
     */
    public void registerUser(User user) {
        // 비밀번호 암호화
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPwd());
        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * 사용자 정보 수정
     */
    public void updateUser(User user) {
        User existingUser = userQueryService.findById(user.getId());
        if (existingUser != null) {
            // 비밀번호가 변경된 경우에만 암호화
            if (user.getPwd() != null && !user.getPwd().isEmpty() &&
                    !user.getPwd().equals(existingUser.getPwd())) {
                String encodedPassword = bCryptPasswordEncoder.encode(user.getPwd());
                user.updatePassword(encodedPassword);
            } else {
                user.updatePassword(existingUser.getPwd());
            }
        }
        userRepository.save(user);
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
