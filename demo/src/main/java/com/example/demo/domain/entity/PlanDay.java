package com.example.demo.domain.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_days")
public class PlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int dayIndex;

    @Column(nullable = false)
    private String dayOfWeek;

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<PlanExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<PlanExercise> exercises) {
        this.exercises = exercises;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;


    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC")
    private List<PlanExercise> exercises = new ArrayList<>();

    public void addExercise(PlanExercise ex){
        exercises.add(ex);
        ex.setDay(this);
    }
}
