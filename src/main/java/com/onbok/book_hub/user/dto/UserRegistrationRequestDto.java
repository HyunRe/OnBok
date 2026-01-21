package com.onbok.book_hub.user.dto;

public record UserRegistrationRequestDto(String pwd, String pwd2, String uname, String email, String profileUrl) {
}