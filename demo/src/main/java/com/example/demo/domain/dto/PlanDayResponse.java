package com.example.demo.domain.dto;

import java.util.List;

public record PlanDayResponse(
        int dayIndex,
        String title,
        List<PlanExerciseResponse> exercises
) {}
