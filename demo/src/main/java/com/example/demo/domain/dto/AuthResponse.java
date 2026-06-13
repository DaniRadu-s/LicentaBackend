package com.example.demo.domain.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {}
