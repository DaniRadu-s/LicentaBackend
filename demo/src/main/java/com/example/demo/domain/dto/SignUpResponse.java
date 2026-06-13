package com.example.demo.domain.dto;

import java.time.LocalDate;

public record SignUpResponse(
        String id,
        String email,
        String username,
        LocalDate BirthDate,
        String message
) {}

