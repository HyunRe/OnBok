package com.onbok.book_hub.user.presentation;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.common.security.oauth2.MyUserDetails;
import com.onbok.book_hub.user.application.UserCommandService;
import com.onbok.book_hub.user.application.UserQueryService;
import com.onbok.book_hub.user.application.UserRegistrationService;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.dto.UserRegistrationRequestDto;
import com.onbok.book_hub.user.dto.UserUpdateRequestDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/view/users")
@RequiredArgsConstructor
@Slf4j
public class UserViewController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserRegistrationService userRegistrationService;

    @GetMapping("/register")
    public String registerForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String registerProc(@Valid UserRegistrationRequestDto userRegistrationRequestDto, Model model) {
        try {
            userRegistrationService.registerUser(userRegistrationRequestDto);
            model.addAttribute("msg", "회원가입이 완료되었습니다.");
            model.addAttribute("url", "/view/users/login");
            return "common/alertMsg";

        } catch (ExpectedException e) {
            model.addAttribute("msg", "회원가입 실패: " + e.getErrorCode().getMessage());
            model.addAttribute("url", "/view/users/register");
            return "common/alertMsg";

        }
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<User> userList = userQueryService.getUsers();
        model.addAttribute("menu", "user");
        model.addAttribute("userList", userList);
        return "user/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@CurrentUser User user, Model model) {
        try {
            userCommandService.deleteUser(user.getId());
            String msg = "사용자가 삭제되었습니다.";
            String url = "/view/users/list";
            model.addAttribute("msg", msg);
            model.addAttribute("url", url);
            return "common/alertMsg";
        } catch (Exception e) {
            String msg = "사용자 삭제 실패: " + e.getMessage();
            String url = "/view/users/list";
            model.addAttribute("msg", msg);
            model.addAttribute("url", url);
            return "common/alertMsg";
        }
    }

    @GetMapping("/profile")
    public String profile(@CurrentUser User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("menu", "profile");
        return "user/profile";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@CurrentUser User user, Model model) {
        model.addAttribute("user", user);
        return "user/update";
    }

    @PostMapping("/update")
    public String updateProc(@CurrentUser User user, UserUpdateRequestDto userUpdateRequestDto, Model model) {
        try {
            userCommandService.updateUser(user, userUpdateRequestDto);
            User updatedUser = userQueryService.findById(user.getId());

            // SecurityContext 갱신 - 프로필 이미지 등 최신 정보 반영
            MyUserDetails newUserDetails = new MyUserDetails(updatedUser);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    newUserDetails,
                    newUserDetails.getPassword(),
                    newUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            log.info("SecurityContext 업데이트 완료 - userId: {}, uname: {}", updatedUser.getId(), updatedUser.getUname());

            model.addAttribute("msg", "사용자 정보가 수정되었습니다.");
            model.addAttribute("url", "/view/users/profile");
            return "common/alertMsg";

        } catch (Exception e) {
            log.error("사용자 정보 수정 실패 - error: {}", e.getMessage(), e);
            model.addAttribute("msg", "수정 실패: " + e.getMessage());
            model.addAttribute("url", "/view/users/profile");
            return "common/alertMsg";
        }
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return "user/login";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(@CurrentUser User user, Model model) {
        String msg = user.getUname() + "님 환영합니다.";
        String url;

        // 관리자는 관리자 대시보드로, 일반 사용자는 메인 페이지로
        if (user.getRole().name().equals("ADMIN")) {
            url = "/view/admin/dashboard";
        } else {
            url = "/view/bookEs/list";
        }

        model.addAttribute("msg", msg);
        model.addAttribute("url", url);
        return "common/alertMsg";
    }

    @GetMapping("/loginFailure")
    public String loginFailure(Model model) {
        String msg = "잘못 입력하였습니다.";
        String url = "/view/users/login";
        model.addAttribute("msg", msg);
        model.addAttribute("url", url);
        return "common/alertMsg";
    }
}
