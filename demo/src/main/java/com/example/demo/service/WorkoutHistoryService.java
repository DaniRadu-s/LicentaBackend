package com.example.demo.service;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.*;
import com.example.demo.persistence.ExerciseRepository;
import com.example.demo.persistence.PlanRepository;
import com.example.demo.persistence.UserRepository;
import com.example.demo.persistence.WorkoutExerciseHistoryRepository;
import com.example.demo.persistence.WorkoutHistoryRepository;
import com.example.demo.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class WorkoutHistoryService {

    private final WorkoutHistoryRepository workoutHistoryRepo;
    private final WorkoutExerciseHistoryRepository workoutExerciseHistoryRepo;
    private final UserRepository userRepo;
    private final ExerciseRepository exerciseRepo;
    private final PlanRepository planRepo;

    public WorkoutHistoryService(WorkoutHistoryRepository workoutHistoryRepo,
                                 WorkoutExerciseHistoryRepository workoutExerciseHistoryRepo,
                                 UserRepository userRepo,
                                 ExerciseRepository exerciseRepo,
                                 PlanRepository planRepo) {
        this.workoutHistoryRepo = workoutHistoryRepo;
        this.workoutExerciseHistoryRepo = workoutExerciseHistoryRepo;
        this.userRepo = userRepo;
        this.exerciseRepo = exerciseRepo;
        this.planRepo = planRepo;
    }

    private Long currentUserId() {
        String usernameOrEmail = SecurityUtils.getCurrentUsername();
        User user = userRepo.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @Transactional
    public WorkoutHistoryResponse create(WorkoutHistoryCreateRequest request) {
        Long userId = currentUserId();

        Plan plan = null;
        if (request.planId() != null) {
            plan = planRepo.findById(request.planId())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            if (!plan.getUserId().equals(userId)) {
                throw new RuntimeException("Plan does not belong to current user");
            }
        }

        if (request.planDayId() != null) {
            if (plan == null) {
                throw new RuntimeException("planId is required when planDayId is provided");
            }

            boolean dayBelongsToPlan = plan.getDays().stream()
                    .anyMatch(d -> d.getId().equals(request.planDayId()));

            if (!dayBelongsToPlan) {
                throw new RuntimeException("planDayId does not belong to the selected plan");
            }
        }

        WorkoutHistory history = new WorkoutHistory();
        history.setUserId(userId);
        history.setPlanId(request.planId());
        history.setPlanDayId(request.planDayId());
        history.setCompletedAt(request.completedAt() != null ? request.completedAt() : LocalDateTime.now());
        history.setDurationMinutes(request.durationMinutes());
        history.setPerceivedEffort(request.perceivedEffort());
        history.setNotes(request.notes());

        if (request.exercises() != null) {
            for (WorkoutExerciseLogRequest ex : request.exercises()) {
                Exercise exercise = exerciseRepo.findById(ex.exerciseId())
                        .orElseThrow(() -> new RuntimeException("Exercise not found: " + ex.exerciseId()));

                WorkoutExerciseHistory exerciseHistory = new WorkoutExerciseHistory();
                exerciseHistory.setExercise(exercise);
                exerciseHistory.setPlannedSets(ex.plannedSets());
                exerciseHistory.setPlannedReps(ex.plannedReps());
                exerciseHistory.setCompletedSets(ex.completedSets());
                exerciseHistory.setCompletedReps(ex.completedReps());
                exerciseHistory.setWeightKg(ex.weightKg());
                exerciseHistory.setRestSeconds(ex.restSeconds());
                exerciseHistory.setNotes(ex.notes());

                history.addExercise(exerciseHistory);
            }
        }

        WorkoutHistory saved = workoutHistoryRepo.save(history);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkoutHistoryResponse> listForCurrentUser() {
        Long userId = currentUserId();
        return workoutHistoryRepo.findAllByUserIdOrderByCompletedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutProgressResponse getProgressForCurrentUser(
            Long exerciseId,
            LocalDate startDate,
            LocalDate endDate,
            WorkoutProgressMetric metricType
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        Long userId = currentUserId();
        Exercise exercise = exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<WorkoutExerciseHistory> entries = workoutExerciseHistoryRepo
                .findAllByWorkoutUserIdAndExerciseIdAndWorkoutCompletedAtBetweenOrderByWorkoutCompletedAtAsc(
                        userId,
                        exerciseId,
                        start,
                        end
                );

        List<WorkoutProgressPointResponse> points = new ArrayList<>();
        for (WorkoutExerciseHistory entry : entries) {
            Double value = computeMetricValue(entry, metricType);
            if (value == null) {
                continue;
            }

            points.add(new WorkoutProgressPointResponse(
                    entry.getWorkout().getCompletedAt(),
                    value,
                    entry.getWeightKg(),
                    entry.getCompletedReps(),
                    entry.getCompletedSets()
            ));
        }

        points.sort(Comparator.comparing(WorkoutProgressPointResponse::completedAt));

        Double initialValue = points.isEmpty() ? null : points.get(0).value();
        Double finalValue = points.isEmpty() ? null : points.get(points.size() - 1).value();
        Double deltaValue = (initialValue == null || finalValue == null) ? null : finalValue - initialValue;
        Double deltaPercent = (initialValue == null || finalValue == null || initialValue == 0.0)
                ? null
                : ((finalValue - initialValue) / initialValue) * 100.0;
        Double personalRecord = points.stream()
                .map(WorkoutProgressPointResponse::value)
                .max(Double::compareTo)
                .orElse(null);

        WorkoutProgressSummaryResponse summary = new WorkoutProgressSummaryResponse(
                initialValue,
                finalValue,
                deltaValue,
                deltaPercent,
                personalRecord,
                points.size()
        );

        return new WorkoutProgressResponse(
                exercise.getId(),
                exercise.getName(),
                metricType,
                startDate,
                endDate,
                points,
                summary
        );
    }

    private Double computeMetricValue(WorkoutExerciseHistory entry, WorkoutProgressMetric metricType) {
        return switch (metricType) {
            case WEIGHT -> entry.getWeightKg();
            case REPS -> entry.getCompletedReps() == null ? null : entry.getCompletedReps().doubleValue();
            case VOLUME -> {
                if (entry.getWeightKg() == null || entry.getCompletedReps() == null) {
                    yield null;
                }
                int sets = entry.getCompletedSets() == null || entry.getCompletedSets() <= 0 ? 1 : entry.getCompletedSets();
                yield entry.getWeightKg() * entry.getCompletedReps() * sets;
            }
            case ESTIMATED_1RM -> {
                if (entry.getWeightKg() == null || entry.getCompletedReps() == null || entry.getCompletedReps() <= 0) {
                    yield null;
                }
                if (entry.getCompletedReps() >= 37) {
                    yield null;
                }
                yield entry.getWeightKg() * (36.0 / (37.0 - entry.getCompletedReps()));
            }
        };
    }

    private WorkoutHistoryResponse toResponse(WorkoutHistory history) {
        return new WorkoutHistoryResponse(
                history.getId(),
                history.getPlanId(),
                history.getPlanDayId(),
                history.getCompletedAt(),
                history.getDurationMinutes(),
                history.getPerceivedEffort(),
                history.getNotes(),
                history.getExercises().stream()
                        .map(ex -> new WorkoutExerciseLogResponse(
                                ex.getId(),
                                ex.getExercise().getId(),
                                ex.getExercise().getName(),
                                ex.getPlannedSets(),
                                ex.getPlannedReps(),
                                ex.getCompletedSets(),
                                ex.getCompletedReps(),
                                ex.getWeightKg(),
                                ex.getRestSeconds(),
                                ex.getNotes()
                        ))
                        .toList()
        );
    }
}
