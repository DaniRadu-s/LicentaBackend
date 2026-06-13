package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.domain.entity.UserRestriction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneticDeterminismTest {

    private static void setId(Exercise e, long id) throws Exception {
        Field f = Exercise.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(e, id);
    }

    @Test
    void optimizer_deterministic_with_same_seed() throws Exception {
        Exercise e1 = new Exercise(); e1.setName("E1"); e1.setMuscleGroup("CHEST"); e1.setEquipment("GYM"); e1.setType("COMPOUND"); e1.setDifficulty("BEGINNER"); setId(e1,1L);
        Exercise e2 = new Exercise(); e2.setName("E2"); e2.setMuscleGroup("BACK"); e2.setEquipment("GYM"); e2.setType("COMPOUND"); e2.setDifficulty("BEGINNER"); setId(e2,2L);
        List<Exercise> pool = new ArrayList<>(); pool.add(e1); pool.add(e2);

        List<UserRestriction> restrictions = List.of();

        GeneticPlanOptimizer opt1 = new GeneticPlanOptimizer(pool, restrictions, "STRENGTH", "BEGINNER", "GYM", 2, 50, 60, 0.8, 0.2, 2, 3);
        GeneticPlanOptimizer opt2 = new GeneticPlanOptimizer(pool, restrictions, "STRENGTH", "BEGINNER", "GYM", 2, 50, 60, 0.8, 0.2, 2, 3);

        opt1.setRandom(new java.util.Random(42L));
        opt2.setRandom(new java.util.Random(42L));

        PlanChromosome c1 = opt1.optimize();
        PlanChromosome c2 = opt2.optimize();

        assertEquals(c1.getFitnessScore(), c2.getFitnessScore(), 1e-6);
        assertEquals(c1.getTotalExercises(), c2.getTotalExercises());
    }
}
