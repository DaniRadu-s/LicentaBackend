package com.example.demo.domain.dto;

public record PlanExerciseResponse(
        int orderIndex,
        Long exerciseId,
        String exerciseName,
        Integer sets,
        Integer reps,
        Integer restSeconds
) {}
