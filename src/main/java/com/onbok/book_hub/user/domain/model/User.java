package com.onbok.book_hub.user.domain.model;

import com.onbok.book_hub.common.domain.BaseTime;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pwd")
    private String pwd;

    @Column(name = "uname")
    private String uname;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;

    private String profileUrl;

    @Builder
    public User(String pwd, String uname, String email, LoginProvider loginProvider, String profileUrl) {
        this.pwd = pwd;
        this.uname = uname;
        this.email = email;
        this.loginProvider = loginProvider;
        this.profileUrl = profileUrl;
    }

    // 비밀번호 변경 (암호화된 비밀번호를 인자로 받음)
    public void updatePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new ExpectedException(ErrorCode.INVALID_PASSWORD);
        }
        this.pwd = encodedPassword;
    }

    // 프로필 정보 수정 (예시: 이름, 프로필 이미지)
    public void updateProfile(String uname, String profileUrl) {
        this.uname = uname;
        this.profileUrl = profileUrl;
    }
}
