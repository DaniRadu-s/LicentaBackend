package com.example.demo.service.plangenerator;

public class WorkoutConfig {

    private final int minSets;
    private final int maxSets;
    private final int minReps;
    private final int maxReps;
    private final int restSeconds;
    private final String rpeTarget;
    private final int compoundExercises;
    private final int isolationExercises;
    private final boolean includeCardio;

    private WorkoutConfig(Builder builder) {
        this.minSets = builder.minSets;
        this.maxSets = builder.maxSets;
        this.minReps = builder.minReps;
        this.maxReps = builder.maxReps;
        this.restSeconds = builder.restSeconds;
        this.rpeTarget = builder.rpeTarget;
        this.compoundExercises = builder.compoundExercises;
        this.isolationExercises = builder.isolationExercises;
        this.includeCardio = builder.includeCardio;
    }

    public static WorkoutConfig forGoal(String goal, String level) {
        return switch (goal.toUpperCase()) {
            case "STRENGTH" -> strengthConfig(level);
            case "MUSCLE_GAIN", "HYPERTROPHY", "GAIN_MASS" -> hypertrophyConfig(level);
            case "WEIGHT_LOSS", "FAT_LOSS", "LOSE_WEIGHT" -> weightLossConfig(level);
            case "ENDURANCE" -> enduranceConfig(level);
            case "GENERAL_FITNESS" -> generalFitnessConfig(level);
            default -> generalFitnessConfig(level);
        };
    }

    private static WorkoutConfig strengthConfig(String level) {
        return new Builder()
                .minSets(4).maxSets(5)
                .minReps(3).maxReps(6)
                .restSeconds(180)
                .rpeTarget("8-9")
                .compoundExercises(4)
                .isolationExercises(adjustForLevel(2, level))
                .includeCardio(false)
                .build();
    }

    private static WorkoutConfig hypertrophyConfig(String level) {
        return new Builder()
                .minSets(3).maxSets(4)
                .minReps(8).maxReps(12)
                .restSeconds(90)
                .rpeTarget("7-8")
                .compoundExercises(3)
                .isolationExercises(adjustForLevel(3, level))
                .includeCardio(false)
                .build();
    }

    private static WorkoutConfig weightLossConfig(String level) {
        return new Builder()
                .minSets(3).maxSets(3)
                .minReps(12).maxReps(15)
                .restSeconds(45)
                .rpeTarget("6-7")
                .compoundExercises(3)
                .isolationExercises(adjustForLevel(2, level))
                .includeCardio(true)
                .build();
    }

    private static WorkoutConfig enduranceConfig(String level) {
        return new Builder()
                .minSets(2).maxSets(3)
                .minReps(15).maxReps(20)
                .restSeconds(30)
                .rpeTarget("5-6")
                .compoundExercises(2)
                .isolationExercises(adjustForLevel(2, level))
                .includeCardio(true)
                .build();
    }

    private static WorkoutConfig generalFitnessConfig(String level) {
        return new Builder()
                .minSets(3).maxSets(3)
                .minReps(10).maxReps(12)
                .restSeconds(60)
                .rpeTarget("6-7")
                .compoundExercises(3)
                .isolationExercises(adjustForLevel(2, level))
                .includeCardio(true)
                .build();
    }

    private static int adjustForLevel(int base, String level) {
        return switch (level.toUpperCase()) {
            case "BEGINNER" -> Math.max(1, base - 1);
            case "ADVANCED" -> base + 1;
            default -> base;
        };
    }

    public int getMinSets() { return minSets; }
    public int getMaxSets() { return maxSets; }
    public int getMinReps() { return minReps; }
    public int getMaxReps() { return maxReps; }
    public int getRestSeconds() { return restSeconds; }
    public String getRpeTarget() { return rpeTarget; }
    public int getCompoundExercises() { return compoundExercises; }
    public int getIsolationExercises() { return isolationExercises; }
    public boolean isIncludeCardio() { return includeCardio; }

    public int getTotalExercisesPerDay() {
        return compoundExercises + isolationExercises + (includeCardio ? 1 : 0);
    }

    public String getRepsRange() {
        return minReps + "-" + maxReps;
    }

    public static class Builder {
        private int minSets = 3;
        private int maxSets = 3;
        private int minReps = 10;
        private int maxReps = 12;
        private int restSeconds = 60;
        private String rpeTarget = "7";
        private int compoundExercises = 3;
        private int isolationExercises = 2;
        private boolean includeCardio = false;

        public Builder minSets(int val) { minSets = val; return this; }
        public Builder maxSets(int val) { maxSets = val; return this; }
        public Builder minReps(int val) { minReps = val; return this; }
        public Builder maxReps(int val) { maxReps = val; return this; }
        public Builder restSeconds(int val) { restSeconds = val; return this; }
        public Builder rpeTarget(String val) { rpeTarget = val; return this; }
        public Builder compoundExercises(int val) { compoundExercises = val; return this; }
        public Builder isolationExercises(int val) { isolationExercises = val; return this; }
        public Builder includeCardio(boolean val) { includeCardio = val; return this; }

        public WorkoutConfig build() {
            return new WorkoutConfig(this);
        }
    }
}
