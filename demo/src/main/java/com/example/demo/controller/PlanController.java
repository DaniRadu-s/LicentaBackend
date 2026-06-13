package com.example.demo.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.Plan;
import com.example.demo.domain.entity.User;
import com.example.demo.persistence.UserRepository;
import com.example.demo.service.PlanService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;
    private final UserRepository userRepo;

    public PlanController(PlanService planService, UserRepository userRepo) {
        this.planService = planService;
        this.userRepo = userRepo;
    }

    @PostMapping("/generate")
    public PlanResponse generate(Authentication auth) {
        Long userId = getUserId(auth);
        Plan plan = planService.generatePlan(userId);
        return toResponse(plan);
    }

    @GetMapping("/active")
    public PlanResponse active(Authentication auth) {
        Long userId = getUserId(auth);
        Plan plan = planService.getActivePlan(userId);
        return toResponse(plan);
    }

    @GetMapping
    public List<PlanResponse> list(Authentication auth) {
        Long userId = getUserId(auth);
        return planService.listPlans(userId).stream().map(this::toResponse).toList();
    }

    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        User u = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return u.getId();
    }


    private PlanResponse toResponse(Plan p) {
        return new PlanResponse(
                p.getId(),
                p.getGoal(),
                p.getLevel(),
                p.isActive(),
                p.getDays().stream().map(d ->
                        new PlanDayDTO(
                                d.getId(),
                                d.getDayIndex(),
                                d.getDayOfWeek(),
                                d.getExercises().stream().map(pe ->
                                        new PlanExerciseDTO(
                                                pe.getId(),
                                        pe.getExercise().getId(),
                                                pe.getOrderIndex(),
                                                pe.getExercise().getName(),
                                            pe.getExercise().getType(),
                                                pe.getSets(),
                                                pe.getReps(),
                                            pe.getRestSeconds(),
                                            pe.getRecommendedWeightKg(),
                                            pe.getRpeTarget(),
                                            pe.getNotes()
                                        )
                                ).toList()
                        )
                ).toList()
        );
    }
}
