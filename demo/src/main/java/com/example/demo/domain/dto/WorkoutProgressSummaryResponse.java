package com.example.demo.domain.dto;

public record WorkoutProgressSummaryResponse(
        Double initialValue,
        Double finalValue,
        Double deltaValue,
        Double deltaPercent,
        Double personalRecord,
        Integer sessionsCount
) {}
