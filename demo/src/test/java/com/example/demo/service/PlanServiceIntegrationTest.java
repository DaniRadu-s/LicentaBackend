package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PlanServiceIntegrationTest {

    @Autowired
    private PlanService planService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserProfileRepository profileRepo;

    @Autowired
    private UserAvailableDayRepository availableDayRepo;

    @Test
    @Transactional
    void generatePlan_endToEnd_createsPlan() {
        User u = new User();
        u.setUsername("testuser");
        u.setEmail("t@example.com");
        u.setFirstName("T");
        u.setLastName("User");
        u.setPassword("secret");
        u.setBirthDate(LocalDate.of(1990,1,1));
        userRepo.save(u);

        UserProfile p = new UserProfile();
        p.setUserId(u.getId());
        p.setExperienceLevel("BEGINNER");
        p.setPrimaryGoal("STRENGTH");
        p.setEquipment("GYM");
        p.setWeight(75.0);
        profileRepo.save(p);

        availableDayRepo.save(new UserAvailableDay(u.getId(), "MONDAY"));
        availableDayRepo.save(new UserAvailableDay(u.getId(), "WEDNESDAY"));

        // no restrictions

        Plan plan = planService.generatePlan(u.getId());

        assertNotNull(plan);
        assertEquals(u.getId(), plan.getUserId());
        assertTrue(plan.isActive());
        assertFalse(plan.getDays().isEmpty());
        // number of days should not exceed available days
        assertTrue(plan.getDays().size() <= 2);
    }
}
