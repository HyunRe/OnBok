package com.onbok.book_hub.common.security.authentication;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.common.security.oauth2.MyUserDetails;
import com.onbok.book_hub.user.application.UserQueryService;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserQueryService userQueryService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userQueryService.findByEmail(email);

        if (user == null) {
            log.warn("Login 실패: 이메일을 찾을 수 없습닌다. (email: " + email + ")");
            throw new ExpectedException(ErrorCode.USER_NOT_FOUND);
        }

        log.info("login 시도: " + user.getId());
        return new MyUserDetails(user);
    }
}
