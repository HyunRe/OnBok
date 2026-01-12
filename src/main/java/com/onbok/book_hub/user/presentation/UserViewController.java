package com.onbok.book_hub.user.presentation;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.user.application.UserAuthService;
import com.onbok.book_hub.user.application.UserCommandService;
import com.onbok.book_hub.user.application.UserQueryService;
import com.onbok.book_hub.user.application.UserRegistrationService;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.dto.UserLoginRequestDto;
import com.onbok.book_hub.user.dto.UserRegistrationRequestDto;
import com.onbok.book_hub.user.dto.UserUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/view/users")
@RequiredArgsConstructor
public class UserViewController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserAuthService userAuthService;
    private final UserRegistrationService userRegistrationService;

    @GetMapping("/register")
    public String registerForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String registerProc(@Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto) {
        userRegistrationService.registerUser(userRegistrationRequestDto);
        return "redirect:/user/list";
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<User> userList = userQueryService.getUsers();
        model.addAttribute("menu", "user");
        model.addAttribute("userList", userList);
        return "user/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userCommandService.deleteUser(id);
        return "redirect:/user/list";
    }

    @GetMapping("/update/{uid}")
    public String updateForm(@PathVariable Long id, Model model) {
        User user = userQueryService.findById(id);
        model.addAttribute("user", user);
        return "user/update";
    }

    @PostMapping("/update")
    public String updateProc(@Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        userRegistrationService.updateUser(userUpdateRequestDto);
        return "redirect:/user/list";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(@CurrentUser User user, Model model) {

        String msg = user.getUname() + "님 환영합니다.";
        String url = "/bookEs/list";
        model.addAttribute("msg", msg);
        model.addAttribute("url", url);
        return "common/alertMsg";
    }

    @GetMapping("/loginFailure")
    public String loginFailure(Model model) {
        String msg = "잘못 입력하였습니다.";
        String url = "/user/login";
        model.addAttribute("msg", msg);
        model.addAttribute("url", url);
        return "common/alertMsg";
    }
}
