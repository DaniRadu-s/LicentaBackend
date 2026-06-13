package com.example.demo.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_exercises")
public class PlanExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private int orderIndex;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }

    public PlanDay getDay() {
        return day;
    }

    public void setDay(PlanDay day) {
        this.day = day;
    }

    private Integer sets;
    private Integer reps;
    private Integer restSeconds;

    @Column(name = "recommended_weight_kg")
    private Double recommendedWeightKg;

    @Column(name = "rpe_target")
    private String rpeTarget;

    @Column(name = "notes")
    private String notes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_day_id")
    private PlanDay day;

    public String getRpeTarget() {
        return rpeTarget;
    }

    public void setRpeTarget(String rpeTarget) {
        this.rpeTarget = rpeTarget;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getRecommendedWeightKg() {
        return recommendedWeightKg;
    }

    public void setRecommendedWeightKg(Double recommendedWeightKg) {
        this.recommendedWeightKg = recommendedWeightKg;
    }
}
