package com.richa.service;

import java.util.List;

import com.richa.exception.ChatException;
import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.model.Chat;
import com.richa.model.Project;
import com.richa.model.User;

public interface ProjectService {
    Project createProject(Project project, Long userId) throws UserException;



    List<Project> getProjectsByTeam(User user,String category,String tag) throws ProjectException;


    Project getProjectById(Long projectId) throws ProjectException;

    String deleteProject(Long projectId,Long userId) throws UserException;

    Project updateProject(Project updatedProject, Long id) throws ProjectException;

    List<Project> searchProjects(String keyword, User user) throws ProjectException;

    void addUserToProject(Long projectId, Long userId) throws UserException, ProjectException;

    void removeUserFromProject(Long projectId, Long userId) throws UserException, ProjectException;

    Chat getChatByProjectId(Long projectId) throws ProjectException, ChatException;



}

