package com.example.demo.service;

import com.example.demo.domain.entity.Plan;
import com.example.demo.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanServiceRepositoryTest {

    private PlanService planService;
    private ExerciseRepository exerciseRepo;
    private PlanRepository planRepo;
    private UserRepository userRepo;
    private UserProfileRepository userProfileRepo;
    private UserAvailableDayRepository userAvailableDayRepo;
    private UserRestrictionRepository userRestrictionRepo;
    private WorkoutHistoryRepository workoutHistoryRepo;

    @BeforeEach
    void setUp() {
        exerciseRepo = mock(ExerciseRepository.class);
        planRepo = mock(PlanRepository.class);
        userRepo = mock(UserRepository.class);
        userProfileRepo = mock(UserProfileRepository.class);
        userAvailableDayRepo = mock(UserAvailableDayRepository.class);
        userRestrictionRepo = mock(UserRestrictionRepository.class);
        workoutHistoryRepo = mock(WorkoutHistoryRepository.class);

        planService = new PlanService(
                exerciseRepo,
                planRepo,
                userRepo,
                userProfileRepo,
                userAvailableDayRepo,
                userRestrictionRepo,
                workoutHistoryRepo
        );
    }

    @Test
    void getActivePlan_returnsPlanWhenPresent() {
        Plan p = new Plan();
        p.setUserId(1L);
        p.setActive(true);

        when(planRepo.findFirstByUserIdAndActiveTrueOrderByCreationDateDesc(1L))
                .thenReturn(Optional.of(p));

        Plan result = planService.getActivePlan(1L);

        assertSame(p, result);
    }

    @Test
    void listPlans_returnsAll() {
        Plan p1 = new Plan(); p1.setUserId(1L);
        Plan p2 = new Plan(); p2.setUserId(1L);

        when(planRepo.findAllByUserIdOrderByCreationDateDesc(1L)).thenReturn(List.of(p1, p2));

        var list = planService.listPlans(1L);

        assertEquals(2, list.size());
        assertTrue(list.contains(p1));
        assertTrue(list.contains(p2));
    }

    @Test
    void deactivateActivePlan_savesInactivePlan() {
        Plan p = new Plan();
        p.setUserId(1L);
        p.setActive(true);

        when(planRepo.findFirstByUserIdAndActiveTrueOrderByCreationDateDesc(1L))
                .thenReturn(Optional.of(p));

        planService.deactivateActivePlan(1L);

        assertFalse(p.isActive());
        verify(planRepo, times(1)).save(p);
    }
}
