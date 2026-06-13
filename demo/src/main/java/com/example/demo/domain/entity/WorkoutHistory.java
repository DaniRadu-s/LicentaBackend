package com.example.demo.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_history")
public class WorkoutHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long planId;

    @Column
    private Long planDayId;

    @Column(nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();

    @Column
    private Integer durationMinutes;

    @Column
    private Integer perceivedEffort;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkoutExerciseHistory> exercises = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getPlanDayId() {
        return planDayId;
    }

    public void setPlanDayId(Long planDayId) {
        this.planDayId = planDayId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getPerceivedEffort() {
        return perceivedEffort;
    }

    public void setPerceivedEffort(Integer perceivedEffort) {
        this.perceivedEffort = perceivedEffort;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<WorkoutExerciseHistory> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExerciseHistory> exercises) {
        this.exercises = exercises;
    }

    public void addExercise(WorkoutExerciseHistory exercise) {
        exercises.add(exercise);
        exercise.setWorkout(this);
    }
}
