package com.example.demo.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "workout_exercise_history")
public class WorkoutExerciseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workout_id")
    private WorkoutHistory workout;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Column
    private Integer plannedSets;

    @Column
    private Integer plannedReps;

    @Column
    private Integer completedSets;

    @Column
    private Integer completedReps;

    @Column
    private Double weightKg;

    @Column
    private Integer restSeconds;

    @Column(length = 500)
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkoutHistory getWorkout() {
        return workout;
    }

    public void setWorkout(WorkoutHistory workout) {
        this.workout = workout;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public Integer getPlannedSets() {
        return plannedSets;
    }

    public void setPlannedSets(Integer plannedSets) {
        this.plannedSets = plannedSets;
    }

    public Integer getPlannedReps() {
        return plannedReps;
    }

    public void setPlannedReps(Integer plannedReps) {
        this.plannedReps = plannedReps;
    }

    public Integer getCompletedSets() {
        return completedSets;
    }

    public void setCompletedSets(Integer completedSets) {
        this.completedSets = completedSets;
    }

    public Integer getCompletedReps() {
        return completedReps;
    }

    public void setCompletedReps(Integer completedReps) {
        this.completedReps = completedReps;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
