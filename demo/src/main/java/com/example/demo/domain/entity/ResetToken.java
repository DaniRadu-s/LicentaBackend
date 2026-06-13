package com.example.demo.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ResetToken {
    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true)
    private String token;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @OneToOne
    private User user;

    private LocalDateTime expirationDate;
}
