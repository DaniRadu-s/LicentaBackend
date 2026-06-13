package com.example.demo.persistence;

import com.example.demo.domain.entity.WorkoutExerciseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutExerciseHistoryRepository extends JpaRepository<WorkoutExerciseHistory, Long> {
    List<WorkoutExerciseHistory> findAllByWorkoutUserIdAndExerciseIdAndWorkoutCompletedAtBetweenOrderByWorkoutCompletedAtAsc(
            Long userId,
            Long exerciseId,
            LocalDateTime start,
            LocalDateTime end
    );
}
