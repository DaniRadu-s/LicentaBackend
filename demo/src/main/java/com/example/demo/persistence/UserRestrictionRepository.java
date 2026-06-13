package com.example.demo.persistence;

import java.util.List;
import com.example.demo.domain.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {
    void deleteByUserId(Long userId);
    List<UserRestriction> findByUserId(Long userId);
}

