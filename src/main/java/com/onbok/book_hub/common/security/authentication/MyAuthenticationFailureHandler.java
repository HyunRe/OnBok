package com.onbok.book_hub.common.security.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String inputPwd = request.getParameter("pwd");
        String inputEmail = request.getParameter("email");

        log.info("🔥 로그인 실패 email = {}", inputEmail);
        log.info("🔥 로그인 실패 pwd = {}", inputPwd);

        String msg = "아이디 또는 패스워드가 틀렸습니다.";
        String url = "/view/users/login";

        // 예외 메세지 확인
        if (exception.getMessage().contains("사용자를 찾을 수 없습니다.")) {
            msg = exception.getMessage(); // 존재 하지 않는 아이디 입니다.
            url = "/view/users/register";
        }

        // 실패 메세지 전달
        request.getSession().setAttribute("error", msg);

        // 리다이렉션
        response.sendRedirect(url);
    }
}
