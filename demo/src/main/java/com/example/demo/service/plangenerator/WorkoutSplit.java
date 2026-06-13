package com.example.demo.service.plangenerator;

import java.util.*;


public class WorkoutSplit {

    public enum SplitType {
        FULL_BODY,
        UPPER_LOWER,
        PUSH_PULL_LEGS,
        BRO_SPLIT
    }

    private final SplitType splitType;
    private final List<DayMuscleGroups> schedule;

    private WorkoutSplit(SplitType splitType, List<DayMuscleGroups> schedule) {
        this.splitType = splitType;
        this.schedule = schedule;
    }

    public static WorkoutSplit forDays(int availableDays) {
        return switch (availableDays) {
            case 1, 2 -> createFullBodySplit(availableDays);
            case 3 -> createFullBodySplit(3);
            case 4 -> createUpperLowerSplit();
            case 5 -> createPushPullLegsSplit(5);
            case 6, 7 -> createPushPullLegsSplit(6);
            default -> createFullBodySplit(3);
        };
    }

    private static WorkoutSplit createFullBodySplit(int days) {
        List<DayMuscleGroups> schedule = new ArrayList<>();

        List<String[]> rotations = List.of(
            new String[]{"CHEST", "BACK", "LEGS", "CORE"},
            new String[]{"SHOULDERS", "BACK", "LEGS", "CORE"},
            new String[]{"CHEST", "BACK", "LEGS", "ARMS"}
        );

        for (int i = 0; i < days; i++) {
            String[] muscles = rotations.get(i % rotations.size());
            schedule.add(new DayMuscleGroups(
                "Full Body " + (char)('A' + i),
                Arrays.asList(muscles),
                List.of(muscles[0], muscles[2])
            ));
        }

        return new WorkoutSplit(SplitType.FULL_BODY, schedule);
    }

    private static WorkoutSplit createUpperLowerSplit() {
        return new WorkoutSplit(SplitType.UPPER_LOWER, List.of(
            new DayMuscleGroups("Upper A", 
                List.of("CHEST", "BACK", "SHOULDERS", "ARMS"),
                List.of("CHEST", "BACK")),
            new DayMuscleGroups("Lower A", 
                List.of("LEGS", "CORE"),
                List.of("LEGS")),
            new DayMuscleGroups("Upper B", 
                List.of("BACK", "CHEST", "SHOULDERS", "ARMS"),
                List.of("BACK", "SHOULDERS")),
            new DayMuscleGroups("Lower B", 
                List.of("LEGS", "CORE"),
                List.of("LEGS"))
        ));
    }

    private static WorkoutSplit createPushPullLegsSplit(int days) {
        List<DayMuscleGroups> base = List.of(
            new DayMuscleGroups("Push", 
                List.of("CHEST", "SHOULDERS", "TRICEPS"),
                List.of("CHEST", "SHOULDERS")),
            new DayMuscleGroups("Pull", 
                List.of("BACK", "BICEPS", "REAR_DELTS"),
                List.of("BACK")),
            new DayMuscleGroups("Legs", 
                List.of("LEGS", "CORE"),
                List.of("LEGS"))
        );

        List<DayMuscleGroups> schedule = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            schedule.add(base.get(i % 3));
        }

        return new WorkoutSplit(SplitType.PUSH_PULL_LEGS, schedule);
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<DayMuscleGroups> getSchedule() {
        return schedule;
    }

    public DayMuscleGroups getDayConfig(int dayIndex) {
        return schedule.get(dayIndex % schedule.size());
    }

    public static class DayMuscleGroups {
        private final String name;
        private final List<String> muscleGroups;
        private final List<String> primaryMuscles;

        public DayMuscleGroups(String name, List<String> muscleGroups, List<String> primaryMuscles) {
            this.name = name;
            this.muscleGroups = muscleGroups;
            this.primaryMuscles = primaryMuscles;
        }

        public String getName() { return name; }
        public List<String> getMuscleGroups() { return muscleGroups; }
        public List<String> getPrimaryMuscles() { return primaryMuscles; }

        public boolean isPrimaryMuscle(String muscle) {
            return primaryMuscles.contains(muscle.toUpperCase());
        }
    }
}
