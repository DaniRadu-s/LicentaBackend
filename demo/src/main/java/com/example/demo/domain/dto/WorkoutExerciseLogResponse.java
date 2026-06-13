package com.example.demo.domain.dto;

public record WorkoutExerciseLogResponse(
        Long id,
        Long exerciseId,
        String exerciseName,
        Integer plannedSets,
        Integer plannedReps,
        Integer completedSets,
        Integer completedReps,
        Double weightKg,
        Integer restSeconds,
        String notes
) {}
