package com.example.demo.controller;

import com.example.demo.domain.dto.WorkoutHistoryCreateRequest;
import com.example.demo.domain.dto.WorkoutHistoryResponse;
import com.example.demo.domain.dto.WorkoutProgressMetric;
import com.example.demo.domain.dto.WorkoutProgressResponse;
import com.example.demo.service.WorkoutHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/workout-history")
public class WorkoutHistoryController {

    private final WorkoutHistoryService workoutHistoryService;

    public WorkoutHistoryController(WorkoutHistoryService workoutHistoryService) {
        this.workoutHistoryService = workoutHistoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutHistoryResponse create(@RequestBody WorkoutHistoryCreateRequest request) {
        return workoutHistoryService.create(request);
    }

    @GetMapping
    public List<WorkoutHistoryResponse> list() {
        return workoutHistoryService.listForCurrentUser();
    }

    @GetMapping("/progress")
    public WorkoutProgressResponse progress(
            @RequestParam Long exerciseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "VOLUME") WorkoutProgressMetric metricType
    ) {
        return workoutHistoryService.getProgressForCurrentUser(exerciseId, startDate, endDate, metricType);
    }
}
