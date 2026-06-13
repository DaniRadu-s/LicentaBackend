package com.example.demo.domain.dto;

import java.time.LocalDate;

public record SignUpRequest(
        String email,
        String username,
        LocalDate BirthDate,
        String firstName,
        String lastName,
        String password,
        String confirmPassword
) {}

