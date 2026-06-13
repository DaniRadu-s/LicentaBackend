package com.example.demo.service.plangenerator;

import com.example.demo.domain.entity.*;
import java.util.*;

public class RuleBasedPlanGenerator {

    private final ExerciseSelector exerciseSelector;
    private final WorkoutConfig workoutConfig;
    private final WorkoutSplit workoutSplit;
    private final String goal;
    private final String level;

    public RuleBasedPlanGenerator(
            List<Exercise> allExercises,
            List<UserRestriction> restrictions,
            String goal,
            String level,
            int availableDays
    ) {
        this.goal = goal;
        this.level = level;
        this.exerciseSelector = new ExerciseSelector(allExercises, restrictions);
        this.workoutConfig = WorkoutConfig.forGoal(goal, level);
        this.workoutSplit = WorkoutSplit.forDays(availableDays);
    }

    public Plan generate(Long userId, List<UserAvailableDay> availableDays, String equipment) {
        Plan plan = new Plan();
        plan.setUserId(userId);
        plan.setGoal(goal);
        plan.setLevel(level);
        plan.setActive(true);

        availableDays.sort(Comparator.comparingInt(d -> dayOrder(d.getId().getDayOfWeek())));

        for (int i = 0; i < availableDays.size(); i++) {
            UserAvailableDay availableDay = availableDays.get(i);
            String dayOfWeek = availableDay.getId().getDayOfWeek();


            WorkoutSplit.DayMuscleGroups dayConfig = workoutSplit.getDayConfig(i);

            PlanDay planDay = generateDay(i, dayOfWeek, dayConfig, equipment);
            plan.addDay(planDay);
        }

        return plan;
    }

    private PlanDay generateDay(
            int dayIndex,
            String dayOfWeek,
            WorkoutSplit.DayMuscleGroups dayConfig,
            String equipment
    ) {
        PlanDay day = new PlanDay();
        day.setDayIndex(dayIndex);
        day.setDayOfWeek(dayOfWeek);


        List<Exercise> exercises = exerciseSelector.selectForDay(
                dayConfig,
                workoutConfig,
                equipment,
                level
        );


        int orderIndex = 0;
        for (Exercise exercise : exercises) {
            PlanExercise planExercise = createPlanExercise(exercise, orderIndex++);
            day.addExercise(planExercise);
        }

        return day;
    }

    private PlanExercise createPlanExercise(Exercise exercise, int orderIndex) {
        PlanExercise pe = new PlanExercise();
        pe.setExercise(exercise);
        pe.setOrderIndex(orderIndex);


        if ("CARDIO".equalsIgnoreCase(exercise.getType())) {
            pe.setSets(1);
            pe.setReps(900);
            pe.setRestSeconds(0);
            pe.setRpeTarget(workoutConfig.getRpeTarget());
            pe.setNotes("15 minute, intensitate " + describeIntensity(workoutConfig.getRpeTarget()));
        } else if ("COMPOUND".equalsIgnoreCase(exercise.getType())) {

            pe.setSets(workoutConfig.getMaxSets());
            pe.setReps(workoutConfig.getMinReps());
            pe.setRestSeconds(workoutConfig.getRestSeconds() + 30);
            pe.setRpeTarget(workoutConfig.getRpeTarget());
            pe.setNotes(generateExerciseNote(exercise, true));
        } else {

            pe.setSets(workoutConfig.getMinSets());
            pe.setReps(workoutConfig.getMaxReps());
            pe.setRestSeconds(workoutConfig.getRestSeconds());
            pe.setRpeTarget(decreaseRpe(workoutConfig.getRpeTarget()));
            pe.setNotes(generateExerciseNote(exercise, false));
        }

        return pe;
    }

    private String generateExerciseNote(Exercise exercise, boolean isCompound) {
        StringBuilder note = new StringBuilder();
        
        if (isCompound) {
            note.append("Exercițiu principal. ");
            if ("STRENGTH".equalsIgnoreCase(goal)) {
                note.append("Focus pe formă corectă și greutate progresivă.");
            } else if ("MUSCLE_GAIN".equalsIgnoreCase(goal)) {
                note.append("Controlează mișcarea, 2-3 secunde pe faza negativă.");
            }
        } else {
            note.append("Exercițiu auxiliar. ");
            note.append("Concentrează-te pe contracția musculară.");
        }

        return note.toString();
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

    public WorkoutSplit.SplitType getSplitType() {
        return workoutSplit.getSplitType();
    }

    public WorkoutConfig getWorkoutConfig() {
        return workoutConfig;
    }

    public boolean hasRestrictions() {
        return exerciseSelector.hasRestrictions();
    }
}
