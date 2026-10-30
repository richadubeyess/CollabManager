package com.richa.repository;

import com.richa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.richa.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
    //new
    void deleteByUser(User user);
    Optional<PasswordResetToken> findByUser(User user);
}
