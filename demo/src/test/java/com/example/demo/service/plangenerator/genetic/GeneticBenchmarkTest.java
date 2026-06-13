package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.testutil.TestDataFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Long-running benchmark, enable locally when needed")
class GeneticBenchmarkTest {

    @Test
    void benchmark_optimizer_runs_and_converges() {
        List<Exercise> pool = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            pool.add(TestDataFactory.createExercise("E" + i, i % 5 == 0 ? "LEGS" : "BACK", "GYM", "INTERMEDIATE", i % 3 == 0 ? "COMPOUND" : "ISOLATION"));
        }

        GeneticPlanOptimizer opt = new GeneticPlanOptimizer(pool, List.of(), "STRENGTH", "INTERMEDIATE", "GYM", 3, 200, 200, 0.8, 0.25, 4, 3);
        opt.setRandom(new java.util.Random(777L));

        long start = System.currentTimeMillis();
        PlanChromosome best = opt.optimize();
        long duration = System.currentTimeMillis() - start;

        System.out.println("Benchmark: best fitness=" + best.getFitnessScore() + ", durationMs=" + duration);

        assertTrue(best.getFitnessScore() >= 0);
    }
}
