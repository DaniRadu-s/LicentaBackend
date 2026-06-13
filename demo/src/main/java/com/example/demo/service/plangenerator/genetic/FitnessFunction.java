package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.service.plangenerator.WorkoutConfig;

import java.util.*;

public class FitnessFunction {

    private static final double WEIGHT_MUSCLE_BALANCE = 0.30;
    private static final double WEIGHT_VARIETY = 0.20;
    private static final double WEIGHT_EXERCISE_ORDER = 0.15;
    private static final double WEIGHT_GOAL_ALIGNMENT = 0.20;
    private static final double WEIGHT_DAILY_BALANCE = 0.15;

    private final String goal;
    private final String level;
    private final WorkoutConfig config;
    private final Set<String> targetMuscleGroups;

    public FitnessFunction(String goal, String level, Set<String> targetMuscleGroups) {
        this.goal = goal.toUpperCase();
        this.level = level.toUpperCase();
        this.config = WorkoutConfig.forGoal(goal, level);
        this.targetMuscleGroups = targetMuscleGroups;
    }

    public double evaluate(PlanChromosome chromosome) {
        if (chromosome.getTotalExercises() == 0) {
            return 0;
        }

        double muscleBalanceScore = evaluateMuscleBalance(chromosome);
        double varietyScore = evaluateVariety(chromosome);
        double orderScore = evaluateExerciseOrder(chromosome);
        double goalScore = evaluateGoalAlignment(chromosome);
        double dailyBalanceScore = evaluateDailyBalance(chromosome);

        double totalScore = 
            muscleBalanceScore * WEIGHT_MUSCLE_BALANCE +
            varietyScore * WEIGHT_VARIETY +
            orderScore * WEIGHT_EXERCISE_ORDER +
            goalScore * WEIGHT_GOAL_ALIGNMENT +
            dailyBalanceScore * WEIGHT_DAILY_BALANCE;

        return Math.min(100, Math.max(0, totalScore));
    }

    private double evaluateMuscleBalance(PlanChromosome chromosome) {
        Map<String, Integer> distribution = chromosome.getMuscleGroupDistribution();
        
        if (distribution.isEmpty()) return 0;

        double mean = distribution.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        double variance = distribution.values().stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);

        double stdDev = Math.sqrt(variance);

        int coveredGroups = 0;
        for (String target : targetMuscleGroups) {
            if (distribution.containsKey(target) && distribution.get(target) > 0) {
                coveredGroups++;
            }
        }
        double coverageRatio = targetMuscleGroups.isEmpty() ? 1.0 : 
                (double) coveredGroups / targetMuscleGroups.size();

        double balanceScore = 100 * (1 - Math.min(1, stdDev / (mean + 1)));
        double coverageScore = 100 * coverageRatio;

        return (balanceScore * 0.6) + (coverageScore * 0.4);
    }

    private double evaluateVariety(PlanChromosome chromosome) {
        List<Exercise> all = chromosome.getAllExercises();
        Set<Long> unique = chromosome.getAllExerciseIds();

        if (all.isEmpty()) return 0;

        double uniqueRatio = (double) unique.size() / all.size();

        int repeatedPenalty = 0;
        for (Long id : unique) {
            int count = chromosome.countExerciseOccurrences(id);
            if (count > 2) {
                repeatedPenalty += (count - 2) * 10;
            }
        }

        return Math.max(0, uniqueRatio * 100 - repeatedPenalty);
    }

    private double evaluateExerciseOrder(PlanChromosome chromosome) {
        double totalScore = 0;
        int numDays = chromosome.getNumberOfDays();

        for (int day = 0; day < numDays; day++) {
            List<Exercise> exercises = chromosome.getDayExercises(day);
            totalScore += evaluateDayOrder(exercises);
        }

        return numDays > 0 ? totalScore / numDays : 0;
    }

    private double evaluateDayOrder(List<Exercise> exercises) {
        if (exercises.size() <= 1) return 100;

        int correctOrder = 0;
        int totalPairs = 0;

        for (int i = 0; i < exercises.size() - 1; i++) {
            for (int j = i + 1; j < exercises.size(); j++) {
                totalPairs++;
                int priorityI = getTypePriority(exercises.get(i).getType());
                int priorityJ = getTypePriority(exercises.get(j).getType());

                if (priorityI <= priorityJ) {
                    correctOrder++;
                }
            }
        }

        return totalPairs > 0 ? (double) correctOrder / totalPairs * 100 : 100;
    }

    private int getTypePriority(String type) {
        return switch (type.toUpperCase()) {
            case "COMPOUND" -> 1;
            case "ISOLATION" -> 2;
            case "CARDIO" -> 3;
            default -> 2;
        };
    }

    private double evaluateGoalAlignment(PlanChromosome chromosome) {
        Map<String, Integer> typeDistribution = getTypeDistribution(chromosome);
        int total = chromosome.getTotalExercises();
        
        if (total == 0) return 0;

        int compounds = typeDistribution.getOrDefault("COMPOUND", 0);
        int isolations = typeDistribution.getOrDefault("ISOLATION", 0);
        int cardio = typeDistribution.getOrDefault("CARDIO", 0);

        double compoundRatio = (double) compounds / total;
        double cardioRatio = (double) cardio / total;

        double idealCompoundRatio;
        double idealCardioRatio;

        switch (goal) {
            case "STRENGTH" -> {
                idealCompoundRatio = 0.70;
                idealCardioRatio = 0.05;
            }
            case "MUSCLE_GAIN", "HYPERTROPHY", "GAIN_MASS" -> {
                idealCompoundRatio = 0.55;
                idealCardioRatio = 0.05;
            }
            case "WEIGHT_LOSS", "FAT_LOSS", "LOSE_WEIGHT" -> {
                idealCompoundRatio = 0.45;
                idealCardioRatio = 0.20;
            }
            case "ENDURANCE" -> {
                idealCompoundRatio = 0.35;
                idealCardioRatio = 0.30;
            }
            default -> {
                idealCompoundRatio = 0.50;
                idealCardioRatio = 0.10;
            }
        }
        double compoundScore = 100 * (1 - Math.abs(compoundRatio - idealCompoundRatio));
        double cardioScore = 100 * (1 - Math.abs(cardioRatio - idealCardioRatio));

        return (compoundScore * 0.6) + (cardioScore * 0.4);
    }

    private double evaluateDailyBalance(PlanChromosome chromosome) {
        double totalScore = 0;
        int numDays = chromosome.getNumberOfDays();

        for (int day = 0; day < numDays; day++) {
            Map<String, Integer> dayDistribution = chromosome.getMuscleGroupDistributionForDay(day);

            int numGroups = dayDistribution.size();
            double groupScore;
            
            if (numGroups >= 2 && numGroups <= 4) {
                groupScore = 100;
            } else if (numGroups == 1 || numGroups == 5) {
                groupScore = 70;
            } else {
                groupScore = 50;
            }

            int maxExercisesForGroup = dayDistribution.values().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);
            
            int dayTotal = chromosome.getDayExercises(day).size();
            if (dayTotal > 0) {
                double concentrationRatio = (double) maxExercisesForGroup / dayTotal;
                if (concentrationRatio > 0.5) {
                    groupScore *= 0.8;
                }
            }

            totalScore += groupScore;
        }

        return numDays > 0 ? totalScore / numDays : 0;
    }

    private Map<String, Integer> getTypeDistribution(PlanChromosome chromosome) {
        Map<String, Integer> distribution = new HashMap<>();
        for (Exercise ex : chromosome.getAllExercises()) {
            distribution.merge(ex.getType().toUpperCase(), 1, Integer::sum);
        }
        return distribution;
    }

    public String generateReport(PlanChromosome chromosome) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Goal: %s, Level: %s\n", goal, level));
        sb.append(String.format("Total Fitness: %.2f\n\n", evaluate(chromosome)));
        
        sb.append(String.format("Muscle Balance:  %.2f (weight: %.0f%%)\n", 
                evaluateMuscleBalance(chromosome), WEIGHT_MUSCLE_BALANCE * 100));
        sb.append(String.format("Variety:         %.2f (weight: %.0f%%)\n", 
                evaluateVariety(chromosome), WEIGHT_VARIETY * 100));
        sb.append(String.format("Exercise Order:  %.2f (weight: %.0f%%)\n", 
                evaluateExerciseOrder(chromosome), WEIGHT_EXERCISE_ORDER * 100));
        sb.append(String.format("Goal Alignment:  %.2f (weight: %.0f%%)\n", 
                evaluateGoalAlignment(chromosome), WEIGHT_GOAL_ALIGNMENT * 100));
        sb.append(String.format("Daily Balance:   %.2f (weight: %.0f%%)\n", 
                evaluateDailyBalance(chromosome), WEIGHT_DAILY_BALANCE * 100));
        
        sb.append("\nMuscle Distribution: ").append(chromosome.getMuscleGroupDistribution());
        sb.append("\nType Distribution: ").append(getTypeDistribution(chromosome));
        
        return sb.toString();
    }
}
