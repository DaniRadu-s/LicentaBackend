package com.example.demo.service.plangenerator;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.domain.entity.UserRestriction;

import java.util.*;
import java.util.stream.Collectors;

public class ExerciseSelector {

    private static final Map<String, Set<String>> RESTRICTION_MUSCLE_MAP = Map.ofEntries(
        Map.entry("SHOULDER_INJURY", Set.of("SHOULDERS", "CHEST")),
        Map.entry("BACK_INJURY", Set.of("BACK", "CORE")),
        Map.entry("KNEE_INJURY", Set.of("LEGS")),
        Map.entry("WRIST_INJURY", Set.of("CHEST", "SHOULDERS", "ARMS", "TRICEPS", "BICEPS")),
        Map.entry("ELBOW_INJURY", Set.of("ARMS", "TRICEPS", "BICEPS", "CHEST")),
        Map.entry("HIP_INJURY", Set.of("LEGS", "CORE")),
        Map.entry("ANKLE_INJURY", Set.of("LEGS", "CARDIO")),
        Map.entry("NECK_INJURY", Set.of("SHOULDERS", "BACK")),
        Map.entry("LOWER_BACK_PAIN", Set.of("BACK", "LEGS", "CORE")),
        Map.entry("HEART_CONDITION", Set.of("CARDIO"))
    );

    private static final Map<String, Set<String>> RESTRICTION_EXERCISE_BLACKLIST = Map.ofEntries(
        Map.entry("SHOULDER_INJURY", Set.of("Bench Press", "Overhead Press", "Dips")),
        Map.entry("BACK_INJURY", Set.of("Deadlift", "Barbell Row", "Back Squat")),
        Map.entry("KNEE_INJURY", Set.of("Back Squat", "Lunges", "Jump Squat", "Running")),
        Map.entry("LOWER_BACK_PAIN", Set.of("Deadlift", "Back Squat", "Barbell Row"))
    );

    private final List<Exercise> allExercises;
    private final List<UserRestriction> restrictions;
    private final Set<String> restrictedMuscleGroups;
    private final Set<String> blacklistedExerciseNames;

    public ExerciseSelector(List<Exercise> allExercises, List<UserRestriction> restrictions) {
        this.allExercises = allExercises;
        this.restrictions = restrictions != null ? restrictions : List.of();
        this.restrictedMuscleGroups = buildRestrictedMuscleGroups();
        this.blacklistedExerciseNames = buildBlacklistedExercises();
    }

    private Set<String> buildRestrictedMuscleGroups() {
        Set<String> restricted = new HashSet<>();
        for (UserRestriction r : restrictions) {
            String type = r.getRestrictionType();
            String normalizedType = normalizeRestrictionType(type);
            if (normalizedType != null && RESTRICTION_MUSCLE_MAP.containsKey(normalizedType)) {
                restricted.addAll(RESTRICTION_MUSCLE_MAP.get(normalizedType));
            }
        }
        return restricted;
    }

    private Set<String> buildBlacklistedExercises() {
        Set<String> blacklisted = new HashSet<>();
        for (UserRestriction r : restrictions) {
            String type = r.getRestrictionType();
            String normalizedType = normalizeRestrictionType(type);
            if (normalizedType != null && RESTRICTION_EXERCISE_BLACKLIST.containsKey(normalizedType)) {
                blacklisted.addAll(RESTRICTION_EXERCISE_BLACKLIST.get(normalizedType));
            }
        }
        return blacklisted;
    }

    private String normalizeRestrictionType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }

        String normalized = type.trim().toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "SHOULDER" -> "SHOULDER_INJURY";
            case "BACK" -> "BACK_INJURY";
            case "KNEE" -> "KNEE_INJURY";
            case "WRIST" -> "WRIST_INJURY";
            case "ELBOW" -> "ELBOW_INJURY";
            case "HIP" -> "HIP_INJURY";
            case "ANKLE" -> "ANKLE_INJURY";
            case "NECK" -> "NECK_INJURY";
            case "LOWER_BACK", "LOW_BACK" -> "LOWER_BACK_PAIN";
            case "HEART" -> "HEART_CONDITION";
            default -> normalized;
        };
    }

    public boolean isSafeForUser(Exercise exercise) {
        if (blacklistedExerciseNames.contains(exercise.getName())) {
            return false;
        }

        return !restrictedMuscleGroups.contains(exercise.getMuscleGroup().toUpperCase());
    }

    public List<Exercise> getSafeExercises(String equipment, String difficulty) {
        return allExercises.stream()
                .filter(this::isSafeForUser)
                .filter(e -> matchesEquipment(e, equipment))
                .filter(e -> matchesDifficulty(e, difficulty))
                .collect(Collectors.toList());
    }

    public List<Exercise> selectForMuscleGroup(
            String muscleGroup,
            int count,
            String equipment,
            String difficulty,
            Set<Long> alreadyUsedIds
    ) {
        List<Exercise> candidates = getSafeExercises(equipment, difficulty).stream()
                .filter(e -> e.getMuscleGroup().equalsIgnoreCase(muscleGroup) || 
                            matchesMuscleGroupAlias(e.getMuscleGroup(), muscleGroup))
                .filter(e -> !alreadyUsedIds.contains(e.getId()))
                .collect(Collectors.toList());

        candidates.sort((a, b) -> {
            int typeCompare = exerciseTypePriority(a.getType()) - exerciseTypePriority(b.getType());
            if (typeCompare != 0) return typeCompare;
            return a.getName().compareTo(b.getName());
        });

        List<Exercise> compounds = candidates.stream()
                .filter(e -> "COMPOUND".equalsIgnoreCase(e.getType()))
                .collect(Collectors.toList());
        List<Exercise> isolations = candidates.stream()
                .filter(e -> "ISOLATION".equalsIgnoreCase(e.getType()))
                .collect(Collectors.toList());

        Collections.shuffle(compounds);
        Collections.shuffle(isolations);

        List<Exercise> result = new ArrayList<>();
        result.addAll(compounds);
        result.addAll(isolations);

        return result.stream().limit(count).collect(Collectors.toList());
    }

    public List<Exercise> selectForDay(
            WorkoutSplit.DayMuscleGroups dayConfig,
            WorkoutConfig workoutConfig,
            String equipment,
            String difficulty
    ) {
        List<Exercise> selected = new ArrayList<>();
        Set<Long> usedIds = new HashSet<>();

        int compoundsNeeded = workoutConfig.getCompoundExercises();
        int isolationsNeeded = workoutConfig.getIsolationExercises();

        for (String primaryMuscle : dayConfig.getPrimaryMuscles()) {
            List<Exercise> compounds = selectForMuscleGroup(
                    primaryMuscle, 
                    Math.max(1, compoundsNeeded / dayConfig.getPrimaryMuscles().size()),
                    equipment, 
                    difficulty, 
                    usedIds
            ).stream()
                    .filter(e -> "COMPOUND".equalsIgnoreCase(e.getType()))
                    .toList();

            for (Exercise ex : compounds) {
                if (selected.size() < compoundsNeeded) {
                    selected.add(ex);
                    usedIds.add(ex.getId());
                }
            }
        }

        for (String muscle : dayConfig.getMuscleGroups()) {
            List<Exercise> isolations = selectForMuscleGroup(
                    muscle,
                    1,
                    equipment,
                    difficulty,
                    usedIds
            ).stream()
                    .filter(e -> "ISOLATION".equalsIgnoreCase(e.getType()) || 
                                 !dayConfig.isPrimaryMuscle(muscle))
                    .toList();

            for (Exercise ex : isolations) {
                if (selected.size() < compoundsNeeded + isolationsNeeded) {
                    selected.add(ex);
                    usedIds.add(ex.getId());
                }
            }
        }

        if (workoutConfig.isIncludeCardio()) {
            selectForMuscleGroup("CARDIO", 1, equipment, difficulty, usedIds)
                    .stream()
                    .findFirst()
                    .ifPresent(selected::add);
        }

        return selected;
    }

    private boolean matchesEquipment(Exercise e, String userEquipment) {
        String exEquip = e.getEquipment().toUpperCase();
        String userEquip = userEquipment.toUpperCase();

        if ("BOTH".equals(exEquip)) return true;

        if ("GYM".equals(userEquip)) return true;
        
        return exEquip.equals(userEquip);
    }

    private boolean matchesDifficulty(Exercise e, String userLevel) {
        String exDiff = e.getDifficulty().toUpperCase();
        String userDiff = userLevel.toUpperCase();

        if ("BEGINNER".equals(userDiff)) {
            return "BEGINNER".equals(exDiff);
        }

        if ("INTERMEDIATE".equals(userDiff)) {
            return "BEGINNER".equals(exDiff) || "INTERMEDIATE".equals(exDiff);
        }

        return true;
    }

    private boolean matchesMuscleGroupAlias(String exerciseMuscle, String targetMuscle) {
        Map<String, Set<String>> aliases = Map.of(
            "ARMS", Set.of("BICEPS", "TRICEPS"),
            "BICEPS", Set.of("ARMS"),
            "TRICEPS", Set.of("ARMS"),
            "SHOULDERS", Set.of("REAR_DELTS"),
            "REAR_DELTS", Set.of("SHOULDERS", "BACK")
        );

        Set<String> targetAliases = aliases.getOrDefault(targetMuscle.toUpperCase(), Set.of());
        return targetAliases.contains(exerciseMuscle.toUpperCase());
    }

    private int exerciseTypePriority(String type) {
        return switch (type.toUpperCase()) {
            case "COMPOUND" -> 1;
            case "ISOLATION" -> 2;
            case "CARDIO" -> 3;
            default -> 4;
        };
    }

    public Set<String> getRestrictedMuscleGroups() {
        return restrictedMuscleGroups;
    }

    public boolean hasRestrictions() {
        return !restrictions.isEmpty();
    }
}
