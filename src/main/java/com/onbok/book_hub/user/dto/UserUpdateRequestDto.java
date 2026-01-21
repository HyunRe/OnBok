package com.onbok.book_hub.user.dto;

public record UserUpdateRequestDto(Long id, String pwd, String pwd2, String uname, String email, String profileUrl, String role, String provider) {
}
