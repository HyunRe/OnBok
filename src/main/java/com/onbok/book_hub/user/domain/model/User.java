package com.onbok.book_hub.user.domain.model;

import com.onbok.book_hub.common.domain.BaseTime;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pwd;
    private String uname;
    private String email;
    private LocalDate regDate;
    private String role;

    // 외부 인증 서비스에 사용
    private String provider;
    private String profileUrl;

    @Builder
    public User(String pwd, String uname, String email, LocalDate regDate, String role, String provider, String profileUrl) {
        this.pwd = pwd;
        this.uname = uname;
        this.email = email;
        this.regDate = regDate;
        this.role = role;
        this.provider = provider;
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
    public void updateProfile(String uname, String email, String profileUrl, String role, String provider) {
        this.uname = uname;
        this.email = email;
        this.profileUrl = profileUrl;
        this.role = role;
        this.provider = provider;
    }
}
