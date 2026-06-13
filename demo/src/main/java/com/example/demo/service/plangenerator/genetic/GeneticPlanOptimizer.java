package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.domain.entity.UserRestriction;
import com.example.demo.service.plangenerator.ExerciseSelector;
import com.example.demo.service.plangenerator.WorkoutConfig;
import com.example.demo.service.plangenerator.WorkoutSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GeneticPlanOptimizer {

    private static final Logger log = LoggerFactory.getLogger(GeneticPlanOptimizer.class);

    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final double mutationRate;
    private final int eliteCount;
    private final int tournamentSize;

    private final List<Exercise> exercisePool;
    private final ExerciseSelector exerciseSelector;
    private final WorkoutConfig workoutConfig;
    private final WorkoutSplit workoutSplit;
    private final FitnessFunction fitnessFunction;
    private final GeneticOperators operators;
    private final String equipment;

    private Random random = new Random();

    // Allow tests to inject a seeded Random for deterministic runs
    public void setRandom(Random random) {
        this.random = random == null ? new Random() : random;
    }

    public GeneticPlanOptimizer(
            List<Exercise> allExercises,
            List<UserRestriction> restrictions,
            String goal,
            String level,
            String equipment,
            int availableDays
    ) {
        this(allExercises, restrictions, goal, level, equipment, availableDays,
                             300,
                             600,
             0.8,
               0.20,
               4,
             3);
    }

    public GeneticPlanOptimizer(
            List<Exercise> allExercises,
            List<UserRestriction> restrictions,
            String goal,
            String level,
            String equipment,
            int availableDays,
            int populationSize,
            int maxGenerations,
            double crossoverRate,
            double mutationRate,
            int eliteCount,
            int tournamentSize
    ) {
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.eliteCount = eliteCount;
        this.tournamentSize = tournamentSize;
        this.equipment = equipment;

        this.exerciseSelector = new ExerciseSelector(allExercises, restrictions);
        this.workoutConfig = WorkoutConfig.forGoal(goal, level);
        this.workoutSplit = WorkoutSplit.forDays(availableDays);

        this.exercisePool = exerciseSelector.getSafeExercises(equipment, level);

        Set<String> targetMuscles = workoutSplit.getSchedule().stream()
                .flatMap(d -> d.getMuscleGroups().stream())
                .collect(Collectors.toSet());

        this.fitnessFunction = new FitnessFunction(goal, level, targetMuscles);
        this.operators = new GeneticOperators(exercisePool);
    }

    public PlanChromosome optimize() {
        log.info("Starting genetic optimization: population={}, generations={}, exercises={}",
                populationSize, maxGenerations, exercisePool.size());

        if (exercisePool.isEmpty()) {
            log.warn("No exercises available in pool!");
            return new PlanChromosome(workoutSplit.getSchedule().size());
        }

        List<PlanChromosome> population = initializePopulation();

        evaluatePopulation(population);
        Collections.sort(population);

        PlanChromosome bestEver = population.get(0).copy();
        int stagnantGenerations = 0;
        double previousBestFitness = bestEver.getFitnessScore();
        int minGenerationsBeforeEarlyStop = Math.max(80, maxGenerations / 3);

        log.info("Initial best fitness: {}", String.format("%.2f", bestEver.getFitnessScore()));

        for (int generation = 0; generation < maxGenerations; generation++) {
            List<PlanChromosome> newPopulation = new ArrayList<>();

            for (int i = 0; i < eliteCount && i < population.size(); i++) {
                newPopulation.add(population.get(i).copy());
            }

            while (newPopulation.size() < populationSize) {

                PlanChromosome parent1 = operators.tournamentSelect(population, tournamentSize);
                PlanChromosome parent2 = operators.tournamentSelect(population, tournamentSize);

                List<PlanChromosome> children;
                if (random.nextDouble() < crossoverRate) {
                    children = operators.uniformCrossover(parent1, parent2);
                } else {
                    children = List.of(parent1.copy(), parent2.copy());
                }

                for (PlanChromosome child : children) {
                    operators.applyMutations(child, mutationRate);
                    if (newPopulation.size() < populationSize) {
                        newPopulation.add(child);
                    }
                }
            }

            evaluatePopulation(newPopulation);
            Collections.sort(newPopulation);

            population = newPopulation;

            if (population.get(0).getFitnessScore() > bestEver.getFitnessScore()) {
                bestEver = population.get(0).copy();
                stagnantGenerations = 0;
            } else {
                stagnantGenerations++;
            }

            if (generation % 20 == 0 || generation == maxGenerations - 1) {
                log.debug("Generation {}: best={}, avg={}", 
                        generation,
                        String.format("%.2f", population.get(0).getFitnessScore()),
                        String.format("%.2f", calculateAverageFitness(population)));
            }

            if (generation >= minGenerationsBeforeEarlyStop && stagnantGenerations > 100) {
                log.info("Early stopping at generation {} due to stagnation", generation);
                break;
            }

            if (generation >= minGenerationsBeforeEarlyStop && bestEver.getFitnessScore() > 95) {
                log.info("Excellent fitness achieved at generation {}", generation);
                break;
            }
        }

        log.info("Optimization complete. Best fitness: {}", 
                String.format("%.2f", bestEver.getFitnessScore()));

        return bestEver;
    }

    private List<PlanChromosome> initializePopulation() {
        List<PlanChromosome> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            PlanChromosome chromosome = createRandomChromosome();
            population.add(chromosome);
        }

        return population;
    }

    private PlanChromosome createRandomChromosome() {
        int numDays = workoutSplit.getSchedule().size();
        PlanChromosome chromosome = new PlanChromosome(numDays);

        Set<Long> usedExerciseIds = new HashSet<>();

        for (int day = 0; day < numDays; day++) {
            WorkoutSplit.DayMuscleGroups dayConfig = workoutSplit.getDayConfig(day);
            List<Exercise> dayExercises = new ArrayList<>();

            int exercisesNeeded = workoutConfig.getTotalExercisesPerDay();

            for (String muscleGroup : dayConfig.getMuscleGroups()) {
                if (dayExercises.size() >= exercisesNeeded) break;

                List<Exercise> candidates = exercisePool.stream()
                        .filter(e -> e.getMuscleGroup().equalsIgnoreCase(muscleGroup))
                        .filter(e -> !usedExerciseIds.contains(e.getId()))
                        .collect(Collectors.toList());

                if (!candidates.isEmpty()) {
                    Collections.shuffle(candidates, random);
                    Exercise selected = candidates.get(0);
                    dayExercises.add(selected);
                    usedExerciseIds.add(selected.getId());
                }
            }

            while (dayExercises.size() < exercisesNeeded) {
                List<Exercise> remaining = exercisePool.stream()
                        .filter(e -> !usedExerciseIds.contains(e.getId()))
                        .collect(Collectors.toList());

                if (remaining.isEmpty()) {
                    remaining = new ArrayList<>(exercisePool);
                }

                if (!remaining.isEmpty()) {
                    Collections.shuffle(remaining, random);
                    Exercise selected = remaining.get(0);
                    dayExercises.add(selected);
                    usedExerciseIds.add(selected.getId());
                } else {
                    break;
                }
            }

            dayExercises.sort((a, b) -> {
                int pA = getTypePriority(a.getType());
                int pB = getTypePriority(b.getType());
                return Integer.compare(pA, pB);
            });

            chromosome.setDayExercises(day, dayExercises);
        }

        return chromosome;
    }

    private int getTypePriority(String type) {
        return switch (type.toUpperCase()) {
            case "COMPOUND" -> 1;
            case "ISOLATION" -> 2;
            case "CARDIO" -> 3;
            default -> 2;
        };
    }

    private void evaluatePopulation(List<PlanChromosome> population) {
        for (PlanChromosome chromosome : population) {
            if (!chromosome.hasFitnessCalculated()) {
                double fitness = fitnessFunction.evaluate(chromosome);
                chromosome.setFitnessScore(fitness);
            }
        }
    }

    private double calculateAverageFitness(List<PlanChromosome> population) {
        return population.stream()
                .mapToDouble(PlanChromosome::getFitnessScore)
                .average()
                .orElse(0);
    }

    public String getOptimizationReport(PlanChromosome best) {
        return fitnessFunction.generateReport(best);
    }

    public int getPopulationSize() { return populationSize; }
    public int getMaxGenerations() { return maxGenerations; }
    public WorkoutSplit.SplitType getSplitType() { return workoutSplit.getSplitType(); }
    public WorkoutConfig getWorkoutConfig() { return workoutConfig; }
}
