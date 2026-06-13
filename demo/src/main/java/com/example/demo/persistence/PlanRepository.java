package com.example.demo.persistence;

import com.example.demo.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findFirstByUserIdAndActiveTrueOrderByCreationDateDesc(Long userId);
    List<Plan> findAllByUserIdOrderByCreationDateDesc(Long userId);
}
