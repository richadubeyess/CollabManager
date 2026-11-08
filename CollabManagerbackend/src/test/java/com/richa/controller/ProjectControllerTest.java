
package com.richa.controller;

import com.richa.exception.ChatException;
import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.exception.MailsException;

import com.richa.model.Chat;
import com.richa.model.Invitation;
import com.richa.model.Project;
import com.richa.model.User;

import com.richa.request.ProjectInvitationRequest;

import com.richa.response.MessageResponse;

import com.richa.service.InvitationService;
import com.richa.service.ProjectService;
import com.richa.service.UserService;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private User user;

    @Mock
    private Project project;

    @Test
    void getProjects_returnsProjects() throws Exception {

        List<Project> projects = Arrays.asList(
                mock(Project.class),
                mock(Project.class)
        );

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(projectService.getProjectsByTeam(user, "Development", "Java"))
                .thenReturn(projects);

        ResponseEntity<List<Project>> response =
                projectController.getProjects(
                        "Development", "Java", "Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(projectService)
                .getProjectsByTeam(user, "Development", "Java");
    }

    @Test
    void getProjectById_whenProjectExists_returnsProject()
            throws Exception {

        when(projectService.getProjectById(1L))
                .thenReturn(project);

        ResponseEntity<Project> response =
                projectController.getProjectById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(project, response.getBody());
    }

    @Test
    void getProjectById_whenProjectDoesNotExist_returnsNotFound()
            throws Exception {

        when(projectService.getProjectById(1L))
                .thenReturn(null);

        ResponseEntity<Project> response =
                projectController.getProjectById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createProject_returnsCreatedProject() throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(projectService.createProject(project, 1L))
                .thenReturn(project);

        ResponseEntity<Project> response =
                projectController.createProject(
                        project, "Bearer token");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(project, response.getBody());

        verify(project).setOwner(user);

        verify(userService).updateUsersProjectSize(user, 1);
    }

    @Test
    void updateProject_whenProjectExists_returnsUpdatedProject()
            throws Exception {

        Project updatedProject = mock(Project.class);

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(projectService.updateProject(updatedProject, 1L))
                .thenReturn(updatedProject);

        ResponseEntity<Project> response =
                projectController.updateProject(
                        updatedProject, 1L, "Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProject, response.getBody());
    }

    @Test
    void updateProject_whenProjectDoesNotExist_returnsNotFound()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(projectService.updateProject(project, 1L))
                .thenReturn(null);

        ResponseEntity<Project> response =
                projectController.updateProject(
                        project, 1L, "Bearer token");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteProject_returnsSuccessMessage() throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(projectService.deleteProject(1L, 1L))
                .thenReturn("Project deleted successfully");

        ResponseEntity<MessageResponse> response =
                projectController.deleteProject(
                        1L, "Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(userService).updateUsersProjectSize(user, -1);
    }

    @Test
    void searchProjects_returnsMatchingProjects() throws Exception {

        List<Project> projects =
                Collections.singletonList(project);

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(projectService.searchProjects("CollabManager", user))
                .thenReturn(projects);

        ResponseEntity<List<Project>> response =
                projectController.searchProjects(
                        "CollabManager", "Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void addUserToProject_returnsSuccessMessage() throws Exception {

        ResponseEntity<MessageResponse> response =
                projectController.addUserToProject(2L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(projectService).addUserToProject(1L, 2L);
    }

    @Test
    void getChatByProjectId_whenChatExists_returnsChat()
            throws Exception {

        Chat chat = mock(Chat.class);

        when(projectService.getChatByProjectId(1L))
                .thenReturn(chat);

        ResponseEntity<Chat> response =
                projectController.getChatByProjectId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chat, response.getBody());
    }

    @Test
    void getChatByProjectId_whenChatDoesNotExist_returnsNotFound()
            throws Exception {

        when(projectService.getChatByProjectId(1L))
                .thenReturn(null);

        ResponseEntity<Chat> response =
                projectController.getChatByProjectId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void inviteToProject_sendsInvitation()
            throws MailsException, MessagingException {

        ProjectInvitationRequest request =
                mock(ProjectInvitationRequest.class);

        when(request.getEmail())
                .thenReturn("test@example.com");

        when(request.getProjectId())
                .thenReturn(1L);

        ResponseEntity<MessageResponse> response =
                projectController.inviteToProject(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(invitationService)
                .sendInvitation("test@example.com", 1L);
    }

    @Test
    void acceptInvitation_returnsAcceptedInvitation()
            throws Exception {

        Invitation invitation = mock(Invitation.class);

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getId()).thenReturn(2L);

        when(invitationService.acceptInvitation("test-token", 2L))
                .thenReturn(invitation);

        when(invitation.getProjectId()).thenReturn(1L);

        ResponseEntity<Invitation> response =
                projectController.acceptInvitation(
                        "test-token", "Bearer token");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(invitation, response.getBody());

        verify(projectService).addUserToProject(1L, 2L);
    }
}
