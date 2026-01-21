package com.onbok.book_hub.common.security.config;

import com.onbok.book_hub.common.security.authentication.MyUserDetailsService;
import com.onbok.book_hub.common.security.jwt.JwtRequestFilter;
import com.onbok.book_hub.common.security.jwt.JwtTokenUtil;
import com.onbok.book_hub.common.security.oauth2.MyOAuth2UserService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final MyOAuth2UserService myOAuth2UserService;
    private final JwtTokenUtil jwtTokenUtil;
    private final MyUserDetailsService myUserDetailsService;

    /**
     * 🔐 Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // H2 콘솔 사용 시 필요
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/view/books/list",
                                "/view/books/detail/**",
                                "/view/bookEs/list",
                                "/view/bookEs/detail/**",
                                "/mall/list",
                                "/mall/detail/**",
                                "/view/users/register",
                                "/view/users/login",
                                "/authenticate",              // ✅ JWT 발급 API
                                "/img/**",
                                "/js/**",
                                "/css/**",
                                "/error/**",
                                "/favicon.ico",
                                "/swagger-ui.html",           // ✅ Swagger UI
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers(
                                "/view/books/insert",
                                "/view/books/yes24",
                                "/api/bookEs/yes24",
                                "/view/orders/listAll",
                                "/view/orders/bookStat",
                                "/view/users/list",
                                "/view/users/delete/**"
                        ).hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )

                // 🧾 Form Login
                .formLogin(form -> form
                        .loginPage("/view/users/login")
                        .loginProcessingUrl("/view/users/login")
                        .usernameParameter("email")
                        .passwordParameter("pwd")
                        .defaultSuccessUrl("/view/users/loginSuccess", true)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )

                // 🚪 Logout
                .logout(logout -> logout
                        .logoutUrl("/view/users/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/view/users/login")
                )

                // 🌐 OAuth2 Login
                .oauth2Login(oauth -> oauth
                        .loginPage("/view/users/login")
                        .userInfoEndpoint(user -> user.userService(myOAuth2UserService))
                        .defaultSuccessUrl("/view/users/loginSuccess", true)
                )

                // 🔥 JWT Filter 등록 (이게 핵심)
                .addFilterBefore(
                        jwtRequestFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * 🔐 JWT Filter Bean
     */
    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter(jwtTokenUtil, myUserDetailsService);
    }

    /**
     * 🔑 AuthenticationManager (JWT 로그인용)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
