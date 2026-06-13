package com.example.demo.domain.dto;

import java.time.LocalDateTime;

public record WorkoutProgressPointResponse(
        LocalDateTime completedAt,
        Double value,
        Double weightKg,
        Integer completedReps,
        Integer completedSets
) {}
