package com.onbok.book_hub.common.aspect;

import com.onbok.book_hub.common.security.oauth2.MyUserDetails;
import com.onbok.book_hub.user.domain.model.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {
    @Before("@annotation(checkPermission)")
    public void checkPermission(JoinPoint joinPoint, CheckPermission checkPermission) throws IllegalAccessException {
        // Spring Security에서 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalAccessException("인증되지 않은 사용자입니다.");
        }

        String requiredPermission = checkPermission.value();

        // MyUserDetails에서 User 정보 가져오기
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        if (currentUser == null || !currentUser.getRole().equals(requiredPermission)) {
            throw new SecurityException("권한 부족: " + requiredPermission);
        }
        System.out.println("권한 검증 통과: " + joinPoint.getSignature());
    }
}