package com.example.demo.domain.dto;

public record WorkoutExerciseLogRequest(
        Long exerciseId,
        Integer plannedSets,
        Integer plannedReps,
        Integer completedSets,
        Integer completedReps,
        Double weightKg,
        Integer restSeconds,
        String notes
) {}
