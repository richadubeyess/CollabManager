package com.richa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.richa.model.Chat;
import com.richa.model.Project;

public interface ChatRepository extends JpaRepository<Chat, Long> {


    Chat findByProject(Project projectById);


}

