package com.example.demo.domain.dto;

public record PlanExerciseDTO(
        Long id,
        Long exerciseId,
        int orderIndex,
        String name,
        String exerciseType,
        Integer sets,
        Integer reps,
        Integer restSeconds,
        Double recommendedWeightKg,
        String rpeTarget,
        String notes
) {}
