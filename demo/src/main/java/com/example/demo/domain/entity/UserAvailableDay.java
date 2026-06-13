package com.example.demo.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_available_days")
public class UserAvailableDay {

    @EmbeddedId
    private UserAvailableDayId id;

    public UserAvailableDay() {}

    public UserAvailableDay(Long userId, String dayOfWeek) {
        this.id = new UserAvailableDayId(userId, dayOfWeek);
    }

    public UserAvailableDayId getId() { return id; }
    public void setId(UserAvailableDayId id) { this.id = id; }
}

