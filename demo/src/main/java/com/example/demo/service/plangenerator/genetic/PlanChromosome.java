package com.example.demo.service.plangenerator.genetic;

import com.example.demo.domain.entity.Exercise;

import java.util.*;

public class PlanChromosome implements Comparable<PlanChromosome> {

    private final List<List<Exercise>> days;
    private double fitnessScore = -1;
    
    public PlanChromosome(int numberOfDays) {
        this.days = new ArrayList<>();
        for (int i = 0; i < numberOfDays; i++) {
            days.add(new ArrayList<>());
        }
    }

    public PlanChromosome(List<List<Exercise>> days) {
        this.days = new ArrayList<>();
        for (List<Exercise> day : days) {
            this.days.add(new ArrayList<>(day));
        }
    }

    public PlanChromosome copy() {
        PlanChromosome copy = new PlanChromosome(this.days);
        copy.fitnessScore = this.fitnessScore;
        return copy;
    }

    public void setDayExercises(int dayIndex, List<Exercise> exercises) {
        if (dayIndex >= 0 && dayIndex < days.size()) {
            days.set(dayIndex, new ArrayList<>(exercises));
            invalidateFitness();
        }
    }

    public List<Exercise> getDayExercises(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < days.size()) {
            return days.get(dayIndex);
        }
        return Collections.emptyList();
    }

    public void replaceExercise(int dayIndex, int exerciseIndex, Exercise newExercise) {
        if (dayIndex >= 0 && dayIndex < days.size()) {
            List<Exercise> dayExercises = days.get(dayIndex);
            if (exerciseIndex >= 0 && exerciseIndex < dayExercises.size()) {
                dayExercises.set(exerciseIndex, newExercise);
                invalidateFitness();
            }
        }
    }

    public void swapExercises(int day1, int ex1, int day2, int ex2) {
        Exercise temp = days.get(day1).get(ex1);
        days.get(day1).set(ex1, days.get(day2).get(ex2));
        days.get(day2).set(ex2, temp);
        invalidateFitness();
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> all = new ArrayList<>();
        for (List<Exercise> day : days) {
            all.addAll(day);
        }
        return all;
    }

    public Set<Long> getAllExerciseIds() {
        Set<Long> ids = new HashSet<>();
        for (List<Exercise> day : days) {
            for (Exercise ex : day) {
                ids.add(ex.getId());
            }
        }
        return ids;
    }

    public boolean containsExercise(Long exerciseId) {
        return getAllExerciseIds().contains(exerciseId);
    }

    public int countExerciseOccurrences(Long exerciseId) {
        int count = 0;
        for (List<Exercise> day : days) {
            for (Exercise ex : day) {
                if (ex.getId().equals(exerciseId)) {
                    count++;
                }
            }
        }
        return count;
    }

    public Map<String, Integer> getMuscleGroupDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        for (List<Exercise> day : days) {
            for (Exercise ex : day) {
                distribution.merge(ex.getMuscleGroup(), 1, Integer::sum);
            }
        }
        return distribution;
    }

    public Map<String, Integer> getMuscleGroupDistributionForDay(int dayIndex) {
        Map<String, Integer> distribution = new HashMap<>();
        if (dayIndex >= 0 && dayIndex < days.size()) {
            for (Exercise ex : days.get(dayIndex)) {
                distribution.merge(ex.getMuscleGroup(), 1, Integer::sum);
            }
        }
        return distribution;
    }

    public int getNumberOfDays() {
        return days.size();
    }

    public int getTotalExercises() {
        return getAllExercises().size();
    }

    public List<List<Exercise>> getDays() {
        return days;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }

    public void setFitnessScore(double fitnessScore) {
        this.fitnessScore = fitnessScore;
    }

    public boolean hasFitnessCalculated() {
        return fitnessScore >= 0;
    }

    private void invalidateFitness() {
        this.fitnessScore = -1;
    }

    @Override
    public int compareTo(PlanChromosome other) {
        return Double.compare(other.fitnessScore, this.fitnessScore);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PlanChromosome[fitness=").append(String.format("%.2f", fitnessScore));
        sb.append(", days=").append(days.size());
        sb.append(", exercises=").append(getTotalExercises());
        sb.append("]");
        return sb.toString();
    }
}
