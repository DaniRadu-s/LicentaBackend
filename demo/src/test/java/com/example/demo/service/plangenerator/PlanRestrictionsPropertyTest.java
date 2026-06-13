package com.example.demo.service.plangenerator;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.domain.entity.UserRestriction;
import com.example.demo.service.plangenerator.genetic.GeneticPlanOptimizer;
import com.example.demo.service.plangenerator.genetic.PlanChromosome;
import com.example.demo.testutil.TestDataFactory;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanRestrictionsPropertyTest {

    @Test
    void multiple_random_runs_respect_knee_restriction() {
        // Restriction 'Knee' should map to muscle group 'LEGS' being restricted
        UserRestriction r = TestDataFactory.createRestriction("Knee");

        for (int iter = 0; iter < 30; iter++) {
            List<Exercise> pool = new ArrayList<>();
            // create a mixture of exercises, some in LEGS and others
            pool.add(TestDataFactory.createExercise("Squat" + iter, "LEGS", "GYM", "BEGINNER", "COMPOUND"));
            pool.add(TestDataFactory.createExercise("Bench" + iter, "CHEST", "GYM", "BEGINNER", "COMPOUND"));
            pool.add(TestDataFactory.createExercise("Row" + iter, "BACK", "GYM", "BEGINNER", "COMPOUND"));
            pool.add(TestDataFactory.createExercise("Plank" + iter, "CORE", "GYM", "BEGINNER", "ISOLATION"));

            GeneticPlanOptimizer opt = new GeneticPlanOptimizer(pool, List.of(r), "STRENGTH", "BEGINNER", "GYM", 2, 40, 30, 0.8, 0.2, 2, 3);
            // seed for repeatability
            opt.setRandom(new java.util.Random(12345 + iter));

            PlanChromosome best = opt.optimize();

            for (var day : best.getDays()) {
                for (Exercise ex : day) {
                    assertNotEquals("LEGS", ex.getMuscleGroup().toUpperCase(), () -> "Restricted muscle group found: " + ex.getName());
                }
            }
        }
    }
}
