package com.example.demo.persistence;

import com.example.demo.domain.entity.WorkoutHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutHistoryRepository extends JpaRepository<WorkoutHistory, Long> {
    List<WorkoutHistory> findAllByUserIdOrderByCompletedAtDesc(Long userId);
}
