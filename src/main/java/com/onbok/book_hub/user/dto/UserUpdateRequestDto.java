package com.onbok.book_hub.user.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateRequestDto {
    private Long id;
    private String pwd;
    private String pwd2;
    private String uname;
    private String email;
    private String profileUrl;
    private String role;
    private String provider;
}
