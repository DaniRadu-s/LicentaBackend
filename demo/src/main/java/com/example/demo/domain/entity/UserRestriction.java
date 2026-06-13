package com.example.demo.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_restrictions")
public class UserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="restriction_type")
    private String restrictionType;

    @Column(columnDefinition="text")
    private String description;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getRestrictionType() { return restrictionType; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setRestrictionType(String restrictionType) { this.restrictionType = restrictionType; }
    public void setDescription(String description) { this.description = description; }
}
