package com.example.demo.persistence;

import com.example.demo.domain.entity.UserAvailableDay;
import com.example.demo.domain.entity.UserAvailableDayId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAvailableDayRepository extends JpaRepository<UserAvailableDay, UserAvailableDayId> {
    void deleteByIdUserId(Long userId);
    List<UserAvailableDay> findByIdUserId(Long userId);
}

