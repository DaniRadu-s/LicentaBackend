package com.example.demo.persistence;

import com.example.demo.domain.entity.ResetToken;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<ResetToken, Long> {
    Optional<ResetToken> findByToken(String token);
    Optional<ResetToken> findByUser(User user);
}
