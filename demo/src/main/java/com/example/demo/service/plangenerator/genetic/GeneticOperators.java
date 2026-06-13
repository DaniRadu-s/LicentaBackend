package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;

import java.util.*;

public class GeneticOperators {

    private Random random = new Random();

    public void setRandom(Random random) {
        this.random = random == null ? new Random() : random;
    }

    private final List<Exercise> exercisePool;

    public GeneticOperators(List<Exercise> exercisePool) {
        this.exercisePool = exercisePool;
    }

    public PlanChromosome tournamentSelect(List<PlanChromosome> population, int tournamentSize) {
        PlanChromosome best = null;
        
        for (int i = 0; i < tournamentSize; i++) {
            PlanChromosome candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.getFitnessScore() > best.getFitnessScore()) {
                best = candidate;
            }
        }
        
        return best;
    }

    public PlanChromosome rouletteSelect(List<PlanChromosome> population) {
        double totalFitness = population.stream()
                .mapToDouble(PlanChromosome::getFitnessScore)
                .sum();
        
        if (totalFitness <= 0) {
            return population.get(random.nextInt(population.size()));
        }

        double threshold = random.nextDouble() * totalFitness;
        double cumulative = 0;
        
        for (PlanChromosome chromosome : population) {
            cumulative += chromosome.getFitnessScore();
            if (cumulative >= threshold) {
                return chromosome;
            }
        }
        
        return population.get(population.size() - 1);
    }

    public List<PlanChromosome> singlePointCrossover(PlanChromosome parent1, PlanChromosome parent2) {
        int numDays = parent1.getNumberOfDays();
        if (numDays <= 1) {
            return List.of(parent1.copy(), parent2.copy());
        }

        int crossoverPoint = 1 + random.nextInt(numDays - 1);

        PlanChromosome child1 = new PlanChromosome(numDays);
        PlanChromosome child2 = new PlanChromosome(numDays);

        for (int day = 0; day < numDays; day++) {
            if (day < crossoverPoint) {
                child1.setDayExercises(day, parent1.getDayExercises(day));
                child2.setDayExercises(day, parent2.getDayExercises(day));
            } else {
                child1.setDayExercises(day, parent2.getDayExercises(day));
                child2.setDayExercises(day, parent1.getDayExercises(day));
            }
        }

        return List.of(child1, child2);
    }

    public List<PlanChromosome> uniformCrossover(PlanChromosome parent1, PlanChromosome parent2) {
        int numDays = parent1.getNumberOfDays();
        
        PlanChromosome child1 = new PlanChromosome(numDays);
        PlanChromosome child2 = new PlanChromosome(numDays);

        for (int day = 0; day < numDays; day++) {
            if (random.nextBoolean()) {
                child1.setDayExercises(day, parent1.getDayExercises(day));
                child2.setDayExercises(day, parent2.getDayExercises(day));
            } else {
                child1.setDayExercises(day, parent2.getDayExercises(day));
                child2.setDayExercises(day, parent1.getDayExercises(day));
            }
        }

        return List.of(child1, child2);
    }

    public List<PlanChromosome> exerciseLevelCrossover(PlanChromosome parent1, PlanChromosome parent2) {
        int numDays = parent1.getNumberOfDays();
        
        PlanChromosome child1 = new PlanChromosome(numDays);
        PlanChromosome child2 = new PlanChromosome(numDays);

        for (int day = 0; day < numDays; day++) {
            List<Exercise> p1Exercises = parent1.getDayExercises(day);
            List<Exercise> p2Exercises = parent2.getDayExercises(day);
            
            List<Exercise> c1Exercises = new ArrayList<>();
            List<Exercise> c2Exercises = new ArrayList<>();

            int maxSize = Math.max(p1Exercises.size(), p2Exercises.size());
            
            for (int i = 0; i < maxSize; i++) {
                Exercise ex1 = i < p1Exercises.size() ? p1Exercises.get(i) : null;
                Exercise ex2 = i < p2Exercises.size() ? p2Exercises.get(i) : null;

                if (random.nextBoolean()) {
                    if (ex1 != null) c1Exercises.add(ex1);
                    if (ex2 != null) c2Exercises.add(ex2);
                } else {
                    if (ex2 != null) c1Exercises.add(ex2);
                    if (ex1 != null) c2Exercises.add(ex1);
                }
            }

            child1.setDayExercises(day, c1Exercises);
            child2.setDayExercises(day, c2Exercises);
        }

        return List.of(child1, child2);
    }

    public void swapMutation(PlanChromosome chromosome) {
        int numDays = chromosome.getNumberOfDays();
        if (numDays == 0) return;

        int day1 = random.nextInt(numDays);
        int day2 = random.nextInt(numDays);

        List<Exercise> ex1List = chromosome.getDayExercises(day1);
        List<Exercise> ex2List = chromosome.getDayExercises(day2);

        if (ex1List.isEmpty() || ex2List.isEmpty()) return;

        int ex1Idx = random.nextInt(ex1List.size());
        int ex2Idx = random.nextInt(ex2List.size());

        chromosome.swapExercises(day1, ex1Idx, day2, ex2Idx);
    }

    public void replaceMutation(PlanChromosome chromosome, String targetMuscleGroup) {
        int numDays = chromosome.getNumberOfDays();
        if (numDays == 0 || exercisePool.isEmpty()) return;

        int dayIdx = random.nextInt(numDays);
        List<Exercise> dayExercises = chromosome.getDayExercises(dayIdx);
        
        if (dayExercises.isEmpty()) return;

        int exIdx = random.nextInt(dayExercises.size());
        Exercise current = dayExercises.get(exIdx);

        List<Exercise> candidates = exercisePool.stream()
                .filter(e -> e.getMuscleGroup().equals(current.getMuscleGroup()))
                .filter(e -> !chromosome.containsExercise(e.getId()))
                .toList();

        if (candidates.isEmpty()) {
            candidates = exercisePool.stream()
                    .filter(e -> !chromosome.containsExercise(e.getId()))
                    .toList();
        }

        if (!candidates.isEmpty()) {
            Exercise replacement = candidates.get(random.nextInt(candidates.size()));
            chromosome.replaceExercise(dayIdx, exIdx, replacement);
        }
    }

    public void shuffleMutation(PlanChromosome chromosome) {
        int numDays = chromosome.getNumberOfDays();
        if (numDays == 0) return;

        int dayIdx = random.nextInt(numDays);
        List<Exercise> dayExercises = new ArrayList<>(chromosome.getDayExercises(dayIdx));
        Collections.shuffle(dayExercises, random);
        chromosome.setDayExercises(dayIdx, dayExercises);
    }

    public void smartOrderMutation(PlanChromosome chromosome) {
        int numDays = chromosome.getNumberOfDays();
        if (numDays == 0) return;

        int dayIdx = random.nextInt(numDays);
        List<Exercise> dayExercises = new ArrayList<>(chromosome.getDayExercises(dayIdx));

        dayExercises.sort((a, b) -> {
            int priorityA = getTypePriority(a.getType());
            int priorityB = getTypePriority(b.getType());
            return Integer.compare(priorityA, priorityB);
        });

        chromosome.setDayExercises(dayIdx, dayExercises);
    }

    private int getTypePriority(String type) {
        return switch (type.toUpperCase()) {
            case "COMPOUND" -> 1;
            case "ISOLATION" -> 2;
            case "CARDIO" -> 3;
            default -> 2;
        };
    }

    public void applyMutations(PlanChromosome chromosome, double mutationRate) {
        if (random.nextDouble() < mutationRate) {
            double r = random.nextDouble();
            if (r < 0.3) {
                swapMutation(chromosome);
            } else if (r < 0.6) {
                replaceMutation(chromosome, null);
            } else if (r < 0.8) {
                shuffleMutation(chromosome);
            } else {
                smartOrderMutation(chromosome);
            }
        }
    }
}
