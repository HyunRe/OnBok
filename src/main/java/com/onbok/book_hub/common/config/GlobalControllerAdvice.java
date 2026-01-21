package com.onbok.book_hub.common.config;

import com.onbok.book_hub.common.security.oauth2.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * @Controller에만 적용되는 전역 설정 (RestController 제외)
 * 세션 대신 SecurityContext의 인증 정보를 모든 뷰에 자동으로 추가
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    /**
     * 모든 뷰에 현재 로그인한 사용자 정보를 자동으로 추가
     */
    @ModelAttribute
    public void addUserInfoToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof MyUserDetails) {

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

            // 사용자 정보를 모델에 추가 (세션 대신 사용)
            model.addAttribute("currentUser", userDetails.getUser());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("isAuthenticated", false);
        }
    }
}
