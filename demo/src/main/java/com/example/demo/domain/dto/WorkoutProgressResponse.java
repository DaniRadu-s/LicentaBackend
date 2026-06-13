package com.example.demo.domain.dto;

import java.time.LocalDate;
import java.util.List;

public record WorkoutProgressResponse(
        Long exerciseId,
        String exerciseName,
        WorkoutProgressMetric metricType,
        LocalDate startDate,
        LocalDate endDate,
        List<WorkoutProgressPointResponse> points,
        WorkoutProgressSummaryResponse summary
) {}
