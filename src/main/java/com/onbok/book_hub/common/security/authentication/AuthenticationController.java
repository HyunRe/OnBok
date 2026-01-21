package com.onbok.book_hub.common.security.authentication;

import com.onbok.book_hub.common.security.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest request) throws Exception {
        AuthenticationManager am = authenticationConfiguration.getAuthenticationManager();
        Authentication authentication = am.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPwd()));
        String jwt = jwtTokenUtil.generateToken(authentication.getName());
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
