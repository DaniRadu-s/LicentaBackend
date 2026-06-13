package com.example.demo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserAvailableDayId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    public UserAvailableDayId() {}

    public UserAvailableDayId(Long userId, String dayOfWeek) {
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
    }

    public Long getUserId() { return userId; }
    public String getDayOfWeek() { return dayOfWeek; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAvailableDayId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(dayOfWeek, that.dayOfWeek);
    }

    @Override public int hashCode() {
        return Objects.hash(userId, dayOfWeek);
    }
}

