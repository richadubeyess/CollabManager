package com.richa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.richa.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findByEmail(String email);

}

