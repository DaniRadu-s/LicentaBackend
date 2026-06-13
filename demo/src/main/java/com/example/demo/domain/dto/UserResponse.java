package com.example.demo.domain.dto;

import java.time.LocalDate;

public record UserResponse(String id, String email, String username, String firstName, String lastName, LocalDate BirthDate) {}
