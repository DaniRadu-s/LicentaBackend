package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.persistence.*;
import com.example.demo.service.plangenerator.WorkoutConfig;
import com.example.demo.service.plangenerator.genetic.GeneticPlanOptimizer;
import com.example.demo.service.plangenerator.genetic.PlanChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final ExerciseRepository exerciseRepo;
    private final PlanRepository planRepo;
    private final UserRepository userRepo;
    private final UserProfileRepository userProfileRepo;
    private final UserAvailableDayRepository userAvailableDayRepo;
    private final UserRestrictionRepository userRestrictionRepo;
    private final WorkoutHistoryRepository workoutHistoryRepo;

    public PlanService(
            ExerciseRepository exerciseRepo,
            PlanRepository planRepo,
            UserRepository userRepo,
            UserProfileRepository userProfileRepo,
            UserAvailableDayRepository userAvailableDayRepo,
            UserRestrictionRepository userRestrictionRepo,
            WorkoutHistoryRepository workoutHistoryRepo
    ) {
        this.exerciseRepo = exerciseRepo;
        this.planRepo = planRepo;
        this.userRepo = userRepo;
        this.userProfileRepo = userProfileRepo;
        this.userAvailableDayRepo = userAvailableDayRepo;
        this.userRestrictionRepo = userRestrictionRepo;
        this.workoutHistoryRepo = workoutHistoryRepo;
    }

    public Plan generatePlan(Long userId) {
        long totalStartNs = System.nanoTime();

        userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<UserAvailableDay> availableDays = userAvailableDayRepo.findByIdUserId(userId);
        if (availableDays == null || availableDays.isEmpty()) {
            throw new RuntimeException("No available days set for user");
        }

        List<Exercise> allExercises = exerciseRepo.findAll();
        List<UserRestriction> restrictions = userRestrictionRepo.findByUserId(userId);

        if (allExercises.isEmpty()) {
            throw new RuntimeException("No exercises available in database");
        }

        String goal = profile.getPrimaryGoal();
        String level = profile.getExperienceLevel();
        String equipment = profile.getEquipment();
        int numDays = availableDays.size();

        log.info("Generating optimized plan for user {} with goal={}, level={}, equipment={}, days={}",
                userId, goal, level, equipment, numDays);

        if (!restrictions.isEmpty()) {
            log.info("User has {} restriction(s): {}", restrictions.size(),
                    restrictions.stream().map(UserRestriction::getRestrictionType).toList());
        }

        GeneticPlanOptimizer optimizer = new GeneticPlanOptimizer(
                allExercises,
                restrictions,
                goal,
                level,
                equipment,
                numDays
        );

        long optimizationStartNs = System.nanoTime();
        PlanChromosome bestChromosome = optimizer.optimize();
        long optimizationMs = (System.nanoTime() - optimizationStartNs) / 1_000_000;

        PrescriptionContext prescriptionContext = buildPrescriptionContext(userId, profile, goal, level);

        log.info("Genetic optimization complete. Best fitness: {}, Split type: {}",
                String.format("%.2f", bestChromosome.getFitnessScore()),
                optimizer.getSplitType());
        log.info("Genetic optimization duration: {} ms", optimizationMs);

        Plan plan = convertChromosomeToPlan(
                bestChromosome,
                userId,
                goal,
                level,
                availableDays,
                optimizer.getWorkoutConfig(),
                prescriptionContext
        );

        deactivateActivePlan(userId);

        Plan savedPlan = planRepo.save(plan);
        long totalMs = (System.nanoTime() - totalStartNs) / 1_000_000;
        log.info("Plan generation total duration: {} ms", totalMs);
        return savedPlan;
    }

    private Plan convertChromosomeToPlan(
            PlanChromosome chromosome,
            Long userId,
            String goal,
            String level,
            List<UserAvailableDay> availableDays,
                WorkoutConfig config,
                PrescriptionContext prescriptionContext
    ) {
        Plan plan = new Plan();
        plan.setUserId(userId);
        plan.setGoal(goal);
        plan.setLevel(level);
        plan.setActive(true);

        availableDays.sort(Comparator.comparingInt(d -> dayOrder(d.getId().getDayOfWeek())));

        for (int i = 0; i < chromosome.getNumberOfDays() && i < availableDays.size(); i++) {
            String dayOfWeek = availableDays.get(i).getId().getDayOfWeek();
            List<Exercise> dayExercises = chromosome.getDayExercises(i);

            PlanDay planDay = new PlanDay();
            planDay.setDayIndex(i);
            planDay.setDayOfWeek(dayOfWeek);
            plan.addDay(planDay);

            int orderIndex = 0;
            for (Exercise exercise : dayExercises) {
                PlanExercise planExercise = createPlanExercise(exercise, orderIndex++, config, goal, level, prescriptionContext);
                planDay.addExercise(planExercise);
            }
        }

        return plan;
    }

    private PlanExercise createPlanExercise(
            Exercise exercise,
            int orderIndex,
            WorkoutConfig config,
            String goal,
            String level,
            PrescriptionContext prescriptionContext
    ) {
        PlanExercise pe = new PlanExercise();
        pe.setExercise(exercise);
        pe.setOrderIndex(orderIndex);

        String exerciseType = exercise.getType().toUpperCase();

        switch (exerciseType) {
            case "CARDIO" -> {
                if (isIntervalCardio(exercise)) {
                    pe.setSets(8);
                    pe.setReps(30);
                    pe.setRestSeconds(45);
                    pe.setRpeTarget(config.getRpeTarget());
                    pe.setNotes("Cardio pe intervale: 8 runde x 30-45 sec efort / 45 sec pauză, intensitate "
                            + describeIntensity(config.getRpeTarget()));
                } else {
                    pe.setSets(1);
                    pe.setReps(900);
                    pe.setRestSeconds(0);
                    pe.setRpeTarget(config.getRpeTarget());
                    pe.setNotes("15 minute, intensitate " + describeIntensity(config.getRpeTarget()));
                }
                pe.setRecommendedWeightKg(null);
            }
            case "COMPOUND" -> {
                pe.setSets(config.getMaxSets());
                pe.setReps(config.getMinReps());
                pe.setRestSeconds(config.getRestSeconds() + 30);
                pe.setRpeTarget(config.getRpeTarget());
                pe.setNotes(generateCompoundNote(goal));
            }
            default -> {
                pe.setSets(config.getMinSets());
                pe.setReps(config.getMaxReps());
                pe.setRestSeconds(config.getRestSeconds());
                pe.setRpeTarget(decreaseRpe(config.getRpeTarget()));
                pe.setNotes("Focus pe contracție musculară controlată.");
            }
        }

        personalizePrescription(pe, exercise, config, goal, level, prescriptionContext);
        applyFatigueAdjustment(pe, config, prescriptionContext);

        return pe;
    }

    private void personalizePrescription(
            PlanExercise pe,
            Exercise exercise,
            WorkoutConfig config,
            String goal,
            String level,
            PrescriptionContext ctx
    ) {
        String type = exercise.getType().toUpperCase();
        if ("CARDIO".equals(type) && !isIntervalCardio(exercise)) {
            return;
        }

        ExerciseHistoryStats exerciseStats = ctx.byExerciseId.get(exercise.getId());
        ExerciseHistoryStats muscleStats = ctx.byMuscleGroup.get(exercise.getMuscleGroup().toUpperCase());

        if ("CARDIO".equals(type)) {
            Integer suggestedSets = pickClosest(
                    exerciseStats != null ? exerciseStats.avgCompletedSets : null,
                    exerciseStats != null ? exerciseStats.avgPlannedSets : null,
                    muscleStats != null ? muscleStats.avgCompletedSets : null,
                    pe.getSets()
            );

            Integer suggestedRest = pickClosest(
                    exerciseStats != null ? exerciseStats.avgRestSeconds : null,
                    muscleStats != null ? muscleStats.avgRestSeconds : null,
                    null,
                    pe.getRestSeconds()
            );

            pe.setSets(clampInt(suggestedSets, 4, 15));
            pe.setRestSeconds(clampInt(suggestedRest, 15, 180));
            return;
        }

        Integer suggestedSets = pickClosest(
                exerciseStats != null ? exerciseStats.avgCompletedSets : null,
                exerciseStats != null ? exerciseStats.avgPlannedSets : null,
                muscleStats != null ? muscleStats.avgCompletedSets : null,
                pe.getSets()
        );

        Integer suggestedReps = pickClosest(
                exerciseStats != null ? exerciseStats.avgCompletedReps : null,
                exerciseStats != null ? exerciseStats.avgPlannedReps : null,
                muscleStats != null ? muscleStats.avgCompletedReps : null,
                pe.getReps()
        );

        Integer suggestedRest = pickClosest(
                exerciseStats != null ? exerciseStats.avgRestSeconds : null,
                muscleStats != null ? muscleStats.avgRestSeconds : null,
                null,
                pe.getRestSeconds()
        );

        pe.setSets(clampInt(adjustForGoalAndLevel(suggestedSets, goal, level, true), 1, 8));
        pe.setReps(clampInt(adjustForGoalAndLevel(suggestedReps, goal, level, false), 1, 30));
        pe.setRestSeconds(clampInt(suggestedRest, Math.max(15, config.getRestSeconds() / 2), 300));

        if (!supportsExternalWeight(exercise)) {
            pe.setRecommendedWeightKg(null);
            return;
        }

        Double baseWeight = pickWeight(exerciseStats, muscleStats, exercise);
        if (baseWeight == null) {
            baseWeight = estimateFromBodyweight(ctx.bodyweightKg, type, level);
        }

        if (baseWeight != null) {
            pe.setRecommendedWeightKg(roundToNearestHalf(adjustWeightForGoal(baseWeight, goal)));
        }
    }

    private boolean supportsExternalWeight(Exercise exercise) {
        String equipment = exercise.getEquipment() == null ? "" : exercise.getEquipment().toUpperCase();
        String name = exercise.getName() == null ? "" : exercise.getName().toLowerCase();

        if ("HOME".equals(equipment)) {
            return false;
        }

        return !(name.contains("bodyweight")
                || name.contains("push-up")
                || name.contains("pull-up")
                || name.contains("chin-up")
                || name.contains("plank")
                || name.contains("crunch")
                || name.contains("mountain climber")
                || name.contains("jumping jack")
                || name.contains("burpee")
                || name.contains("high knees")
                || name.contains("running"));
    }

    private PrescriptionContext buildPrescriptionContext(Long userId, UserProfile profile, String goal, String level) {
        List<WorkoutHistory> history = workoutHistoryRepo.findAllByUserIdOrderByCompletedAtDesc(userId);

        Map<Long, ExerciseHistoryStats> byExercise = aggregateByExercise(history);
        Map<String, ExerciseHistoryStats> byMuscle = aggregateByMuscleGroup(history);

        if (!history.isEmpty()) {
            log.info("History-aware plan generation: {} sessions, {} tracked exercises",
                    history.size(), byExercise.size());
        }

        Double recentAvgEffort = calculateRecentAverageEffort(history, 3);
        return new PrescriptionContext(byExercise, byMuscle, profile.getWeight(), goal, level, recentAvgEffort);
    }

    private Double calculateRecentAverageEffort(List<WorkoutHistory> history, int maxSessions) {
        if (history == null || history.isEmpty()) return null;

        IntSummaryStatistics stats = history.stream()
                .filter(h -> h.getPerceivedEffort() != null)
                .limit(maxSessions)
                .mapToInt(WorkoutHistory::getPerceivedEffort)
                .summaryStatistics();

        if (stats.getCount() == 0) return null;
        return stats.getAverage();
    }

    private void applyFatigueAdjustment(PlanExercise pe, WorkoutConfig config, PrescriptionContext ctx) {
        if (pe == null || ctx == null || ctx.recentAvgEffort == null) return;

        double avgEffort = ctx.recentAvgEffort;
        if (avgEffort < 8.0) return;

        pe.setRpeTarget(decreaseRpe(pe.getRpeTarget()));

        if (pe.getSets() != null) {
            pe.setSets(Math.max(1, pe.getSets() - 1));
        }
        if (pe.getReps() != null) {
            pe.setReps(Math.max(1, pe.getReps() - 1));
        }
        if (pe.getRestSeconds() != null && pe.getRestSeconds() > 0) {
            pe.setRestSeconds(clampInt(pe.getRestSeconds() + 15, Math.max(15, config.getRestSeconds() / 2), 300));
        }

        if (pe.getNotes() != null && !pe.getNotes().contains("oboseală")) {
            pe.setNotes(pe.getNotes() + " (ajustat pentru oboseală ridicată)");
        }
    }

    private Map<Long, ExerciseHistoryStats> aggregateByExercise(List<WorkoutHistory> history) {
        Map<Long, List<WorkoutExerciseHistory>> grouped = history.stream()
                .flatMap(h -> h.getExercises().stream())
                .collect(Collectors.groupingBy(ex -> ex.getExercise().getId()));

        Map<Long, ExerciseHistoryStats> result = new HashMap<>();
        for (Map.Entry<Long, List<WorkoutExerciseHistory>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), ExerciseHistoryStats.from(entry.getValue()));
        }
        return result;
    }

    private Map<String, ExerciseHistoryStats> aggregateByMuscleGroup(List<WorkoutHistory> history) {
        Map<String, List<WorkoutExerciseHistory>> grouped = history.stream()
                .flatMap(h -> h.getExercises().stream())
                .collect(Collectors.groupingBy(ex -> ex.getExercise().getMuscleGroup().toUpperCase()));

        Map<String, ExerciseHistoryStats> result = new HashMap<>();
        for (Map.Entry<String, List<WorkoutExerciseHistory>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), ExerciseHistoryStats.from(entry.getValue()));
        }
        return result;
    }

    private Integer pickClosest(Integer first, Integer second, Integer third, Integer fallback) {
        if (first != null && first > 0) return first;
        if (second != null && second > 0) return second;
        if (third != null && third > 0) return third;
        return fallback;
    }

    private Integer adjustForGoalAndLevel(Integer value, String goal, String level, boolean sets) {
        if (value == null) return null;

        int adjusted = value;
        String normalizedGoal = goal.toUpperCase();
        String normalizedLevel = level.toUpperCase();

        if (sets) {
            if ("STRENGTH".equals(normalizedGoal) || "GAIN_MASS".equals(normalizedGoal) || "MUSCLE_GAIN".equals(normalizedGoal)) {
                adjusted += 1;
            }
            if ("LOSE_WEIGHT".equals(normalizedGoal) || "WEIGHT_LOSS".equals(normalizedGoal)) {
                adjusted = Math.max(2, adjusted);
            }
        } else {
            if ("STRENGTH".equals(normalizedGoal)) {
                adjusted = Math.max(3, adjusted - 2);
            }
            if ("LOSE_WEIGHT".equals(normalizedGoal) || "WEIGHT_LOSS".equals(normalizedGoal)) {
                adjusted += 2;
            }
        }

        if ("BEGINNER".equals(normalizedLevel)) {
            adjusted = sets ? Math.max(2, adjusted - 1) : Math.max(6, adjusted - 1);
        }

        return adjusted;
    }

    private Double pickWeight(ExerciseHistoryStats exerciseStats, ExerciseHistoryStats muscleStats, Exercise exercise) {
        if (exerciseStats != null && exerciseStats.avgWeightKg != null && exerciseStats.avgWeightKg > 0) {
            return exerciseStats.avgWeightKg;
        }
        if (muscleStats != null && muscleStats.avgWeightKg != null && muscleStats.avgWeightKg > 0) {
            String type = exercise.getType().toUpperCase();
            double factor = "COMPOUND".equals(type) ? 1.0 : 0.8;
            return muscleStats.avgWeightKg * factor;
        }
        return null;
    }

    private Double estimateFromBodyweight(Double bodyweightKg, String exerciseType, String level) {
        if (bodyweightKg == null || bodyweightKg <= 0) {
            return null;
        }

        String normalizedLevel = level.toUpperCase();
        double ratio;
        if ("COMPOUND".equals(exerciseType)) {
            ratio = switch (normalizedLevel) {
                case "BEGINNER" -> 0.35;
                case "ADVANCED" -> 0.75;
                default -> 0.50;
            };
        } else {
            ratio = switch (normalizedLevel) {
                case "BEGINNER" -> 0.18;
                case "ADVANCED" -> 0.35;
                default -> 0.25;
            };
        }

        return bodyweightKg * ratio;
    }

    private Double adjustWeightForGoal(Double weightKg, String goal) {
        if (weightKg == null) return null;

        return switch (goal.toUpperCase()) {
            case "STRENGTH" -> weightKg * 1.05;
            case "LOSE_WEIGHT", "WEIGHT_LOSS", "FAT_LOSS" -> weightKg * 0.90;
            default -> weightKg;
        };
    }

    private int clampInt(Integer value, int min, int max) {
        if (value == null) return min;
        return Math.max(min, Math.min(max, value));
    }

    private double roundToNearestHalf(double value) {
        return Math.round(value * 2.0) / 2.0;
    }

    private String generateCompoundNote(String goal) {
        return switch (goal.toUpperCase()) {
            case "STRENGTH" -> "Exercițiu principal. Focus pe formă corectă și greutate progresivă.";
            case "MUSCLE_GAIN", "HYPERTROPHY", "GAIN_MASS" -> "Exercițiu principal. Controlează mișcarea, 2-3 sec pe faza negativă.";
            case "WEIGHT_LOSS", "LOSE_WEIGHT", "FAT_LOSS" -> "Exercițiu principal. Menține tempo moderat, fără pauze lungi.";
            default -> "Exercițiu principal. Concentrează-te pe tehnica corectă.";
        };
    }

    private String describeIntensity(String rpe) {
        return switch (rpe) {
            case "5-6" -> "ușoară";
            case "6-7" -> "moderată";
            case "7-8" -> "moderată spre grea";
            case "8-9" -> "grea";
            case "9-10" -> "maximală";
            default -> "moderată";
        };
    }

    private String decreaseRpe(String rpe) {
        return switch (rpe) {
            case "8-9" -> "7-8";
            case "7-8" -> "6-7";
            case "6-7" -> "5-6";
            default -> rpe;
        };
    }

    private boolean isIntervalCardio(Exercise exercise) {
        if (exercise == null || exercise.getName() == null) {
            return false;
        }

        String name = exercise.getName().toUpperCase();
        return name.contains("JUMPING JACK")
                || name.contains("BURPEE")
                || name.contains("HIGH KNEE")
                || name.contains("JUMP ROPE");
    }

    private int dayOrder(String key) {
        return switch (key.toUpperCase()) {
            case "MONDAY" -> 1;
            case "TUESDAY" -> 2;
            case "WEDNESDAY" -> 3;
            case "THURSDAY" -> 4;
            case "FRIDAY" -> 5;
            case "SATURDAY" -> 6;
            case "SUNDAY" -> 7;
            default -> 99;
        };
    }

    public Plan getActivePlan(Long userId) {
        return planRepo.findFirstByUserIdAndActiveTrueOrderByCreationDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("No active plan"));
    }

    public List<Plan> listPlans(Long userId) {
        return planRepo.findAllByUserIdOrderByCreationDateDesc(userId);
    }

    public void deactivateActivePlan(Long userId) {
        planRepo.findFirstByUserIdAndActiveTrueOrderByCreationDateDesc(userId).ifPresent(p -> {
            p.setActive(false);
            planRepo.save(p);
        });
    }

    private static final class PrescriptionContext {
        private final Map<Long, ExerciseHistoryStats> byExerciseId;
        private final Map<String, ExerciseHistoryStats> byMuscleGroup;
        private final Double bodyweightKg;
        private final Double recentAvgEffort;
        @SuppressWarnings("unused")
        private final String goal;
        @SuppressWarnings("unused")
        private final String level;

        private PrescriptionContext(
                Map<Long, ExerciseHistoryStats> byExerciseId,
                Map<String, ExerciseHistoryStats> byMuscleGroup,
                Double bodyweightKg,
                String goal,
                String level,
                Double recentAvgEffort
        ) {
            this.byExerciseId = byExerciseId;
            this.byMuscleGroup = byMuscleGroup;
            this.bodyweightKg = bodyweightKg;
            this.goal = goal;
            this.level = level;
            this.recentAvgEffort = recentAvgEffort;
        }
    }

    private static final class ExerciseHistoryStats {
        private final Integer avgPlannedSets;
        private final Integer avgPlannedReps;
        private final Integer avgCompletedSets;
        private final Integer avgCompletedReps;
        private final Integer avgRestSeconds;
        private final Double avgWeightKg;

        private ExerciseHistoryStats(
                Integer avgPlannedSets,
                Integer avgPlannedReps,
                Integer avgCompletedSets,
                Integer avgCompletedReps,
                Integer avgRestSeconds,
                Double avgWeightKg
        ) {
            this.avgPlannedSets = avgPlannedSets;
            this.avgPlannedReps = avgPlannedReps;
            this.avgCompletedSets = avgCompletedSets;
            this.avgCompletedReps = avgCompletedReps;
            this.avgRestSeconds = avgRestSeconds;
            this.avgWeightKg = avgWeightKg;
        }

        private static ExerciseHistoryStats from(List<WorkoutExerciseHistory> history) {
            return new ExerciseHistoryStats(
                    avgInt(history.stream().map(WorkoutExerciseHistory::getPlannedSets).toList()),
                    avgInt(history.stream().map(WorkoutExerciseHistory::getPlannedReps).toList()),
                    avgInt(history.stream().map(WorkoutExerciseHistory::getCompletedSets).toList()),
                    avgInt(history.stream().map(WorkoutExerciseHistory::getCompletedReps).toList()),
                    avgInt(history.stream().map(WorkoutExerciseHistory::getRestSeconds).toList()),
                    avgDouble(history.stream().map(WorkoutExerciseHistory::getWeightKg).toList())
            );
        }

        private static Integer avgInt(List<Integer> values) {
            OptionalDouble average = values.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average();

            if (average.isEmpty()) {
                return null;
            }

            return (int) Math.round(average.getAsDouble());
        }

        private static Double avgDouble(List<Double> values) {
            OptionalDouble average = values.stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average();

            if (average.isEmpty()) {
                return null;
            }

            return average.getAsDouble();
        }
    }
}
