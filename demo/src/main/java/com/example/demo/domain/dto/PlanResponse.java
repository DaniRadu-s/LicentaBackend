package com.example.demo.domain.dto;

import java.util.List;

public record PlanResponse(
        Long id,
        String goal,
        String level,
        boolean active,
        List<PlanDayDTO> days
) {}
