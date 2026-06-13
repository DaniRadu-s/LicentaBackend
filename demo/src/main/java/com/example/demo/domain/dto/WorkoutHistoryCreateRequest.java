package com.example.demo.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutHistoryCreateRequest(
        Long planId,
        Long planDayId,
        LocalDateTime completedAt,
        Integer durationMinutes,
        Integer perceivedEffort,
        String notes,
        List<WorkoutExerciseLogRequest> exercises
) {}
