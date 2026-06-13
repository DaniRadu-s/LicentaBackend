package com.example.demo.domain.dto;

public record ResetPasswordRequest (String token, String newPassword) {
}
