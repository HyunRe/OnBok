package com.onbok.book_hub.common.security.authentication;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthenticationRequest {
    private String email;
    public String pwd;
}
