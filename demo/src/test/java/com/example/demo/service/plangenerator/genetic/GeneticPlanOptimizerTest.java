package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.domain.entity.UserRestriction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneticPlanOptimizerTest {

    private static void setId(Exercise e, long id) throws Exception {
        Field f = Exercise.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(e, id);
    }

    @Test
    void optimizer_respects_restrictions() throws Exception {
        // Create exercises (one leg exercise which should be restricted by KNEE restriction)
        Exercise legs = new Exercise();
        legs.setName("Squat");
        legs.setMuscleGroup("LEGS");
        legs.setEquipment("GYM");
        legs.setType("COMPOUND");
        legs.setDifficulty("BEGINNER");
        setId(legs, 1L);

        Exercise shoulders = new Exercise();
        shoulders.setName("Overhead Press");
        shoulders.setMuscleGroup("SHOULDERS");
        shoulders.setEquipment("GYM");
        shoulders.setType("COMPOUND");
        shoulders.setDifficulty("BEGINNER");
        setId(shoulders, 2L);

        Exercise core = new Exercise();
        core.setName("Plank");
        core.setMuscleGroup("CORE");
        core.setEquipment("GYM");
        core.setType("ISOLATION");
        core.setDifficulty("BEGINNER");
        setId(core, 3L);

        List<Exercise> pool = new ArrayList<>();
        pool.add(legs);
        pool.add(shoulders);
        pool.add(core);

        UserRestriction r = new UserRestriction();
        r.setId(1L);
        r.setUserId(1L);
        r.setRestrictionType("Knee"); // should map to LEGS

        List<UserRestriction> restrictions = List.of(r);

        // small population and generations for test speed
        GeneticPlanOptimizer opt = new GeneticPlanOptimizer(
                pool,
                restrictions,
                "STRENGTH",
                "BEGINNER",
                "GYM",
                3,
                20, // populationSize
                30, // maxGenerations
                0.8,
                0.2,
                2,
                3
        );

        PlanChromosome best = opt.optimize();

        assertEquals(3, best.getNumberOfDays());

        // Ensure none of the selected exercises belong to restricted muscle group (LEGS)
        for (var day : best.getDays()) {
            for (Exercise ex : day) {
                assertNotEquals("LEGS", ex.getMuscleGroup().toUpperCase());
            }
        }
    }
}
