package com.example.demo.domain.dto;

import java.util.List;

public record PlanDayDTO(
        Long id,
        int dayIndex,
        String dayOfWeek,
        List<PlanExerciseDTO> exercises
) {}
