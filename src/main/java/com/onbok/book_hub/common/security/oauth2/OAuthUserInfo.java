package com.onbok.book_hub.common.security.oauth2;

import com.onbok.book_hub.user.domain.model.LoginProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthUserInfo {
    private String email;
    private String uname;
    private String profileUrl;
    private LoginProvider provider;

    public OAuthUserInfo(String email, String uname, String profileUrl, LoginProvider provider) {
        this.email = email;
        this.uname = uname;
        this.profileUrl = profileUrl;
        this.provider = provider;
    }
}
