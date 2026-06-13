package com.example.demo.domain.dto;
import java.util.List;

public record UserProfileDTO (
    Integer age,
    Double weight,
    Integer heightCm,
    String sex,
    String experienceLevel,
    String primaryGoal,
    Integer maxWorkoutMinutes,
    String equipment,
    List<String> availableDays,
    List<RestrictionDTO> restrictions
) {}

