
package com.richa.service;

import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.model.Chat;
import com.richa.model.Project;
import com.richa.model.User;
import com.richa.repository.ProjectRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ChatService chatService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User user;
    private Project project;
    private Project secondProject;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        project = new Project();
        project.setName("CollabManager");
        project.setDescription("Project management application");
        project.setCategory("Development");
        project.setTags(Arrays.asList("Java", "Spring Boot"));

        secondProject = new Project();
        secondProject.setName("Banking App");
        secondProject.setDescription("Banking application");
        secondProject.setCategory("Finance");
        secondProject.setTags(Arrays.asList("Java", "MySQL"));
    }

    // 1. Create project successfully
    @Test
    void createProject_whenValidProject_returnsCreatedProject()
            throws Exception {

        when(userService.findUserById(1L))
                .thenReturn(user);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Chat chat = new Chat();

        when(chatService.createChat(any(Chat.class)))
                .thenReturn(chat);

        Project result = projectService.createProject(project, 1L);

        assertNotNull(result);
        assertEquals("CollabManager", result.getName());
        assertEquals("Project management application",
                result.getDescription());
        assertEquals("Development", result.getCategory());

        assertSame(user, result.getOwner());
        assertTrue(result.getTeam().contains(user));
        assertSame(chat, result.getChat());

        verify(userService).findUserById(1L);
        verify(projectRepository).save(any(Project.class));
        verify(chatService).createChat(any(Chat.class));
    }

    // 2. Get project by ID - success
    @Test
    void getProjectById_whenProjectExists_returnsProject()
            throws ProjectException {

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(1L);

        assertNotNull(result);
        assertSame(project, result);

        verify(projectRepository).findById(1L);
    }

    // 3. Get project by ID - not found
    @Test
    void getProjectById_whenProjectDoesNotExist_throwsException() {

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectException.class,
                () -> projectService.getProjectById(99L)
        );

        verify(projectRepository).findById(99L);
    }

    // 4. Get projects by team - without filters
    @Test
    void getProjectsByTeam_withoutFilters_returnsAllProjects()
            throws ProjectException {

        List<Project> projects = Arrays.asList(project, secondProject);

        when(projectRepository.findByTeamContainingOrOwner(user, user))
                .thenReturn(projects);

        List<Project> result =
                projectService.getProjectsByTeam(user, null, null);

        assertEquals(2, result.size());
        assertTrue(result.contains(project));
        assertTrue(result.contains(secondProject));

        verify(projectRepository)
                .findByTeamContainingOrOwner(user, user);
    }

    // 5. Get projects by team - filter by category
    @Test
    void getProjectsByTeam_withCategory_returnsMatchingProjects()
            throws ProjectException {

        List<Project> projects = Arrays.asList(project, secondProject);

        when(projectRepository.findByTeamContainingOrOwner(user, user))
                .thenReturn(projects);

        List<Project> result =
                projectService.getProjectsByTeam(user, "Development", null);

        assertEquals(1, result.size());
        assertEquals("Development", result.get(0).getCategory());
    }

    // 6. Get projects by team - filter by tag
    @Test
    void getProjectsByTeam_withTag_returnsMatchingProjects()
            throws ProjectException {

        List<Project> projects = Arrays.asList(project, secondProject);

        when(projectRepository.findByTeamContainingOrOwner(user, user))
                .thenReturn(projects);

        List<Project> result =
                projectService.getProjectsByTeam(user, null, "Spring Boot");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTags().contains("Spring Boot"));
    }

    // 7. Get projects by team - filter by category and tag
    @Test
    void getProjectsByTeam_withCategoryAndTag_returnsMatchingProjects()
            throws ProjectException {

        List<Project> projects = Arrays.asList(project, secondProject);

        when(projectRepository.findByTeamContainingOrOwner(user, user))
                .thenReturn(projects);

        List<Project> result =
                projectService.getProjectsByTeam(
                        user, "Development", "Java"
                );

        assertEquals(1, result.size());
        assertEquals("Development", result.get(0).getCategory());
        assertTrue(result.get(0).getTags().contains("Java"));
    }

    // 8. Get projects by team - no matching category
    @Test
    void getProjectsByTeam_withNonExistingCategory_returnsEmptyList()
            throws ProjectException {

        when(projectRepository.findByTeamContainingOrOwner(user, user))
                .thenReturn(Arrays.asList(project, secondProject));

        List<Project> result =
                projectService.getProjectsByTeam(user, "Marketing", null);

        assertTrue(result.isEmpty());
    }

    // 9. Get projects by team - no matching tag
    @Test
    void getProjectsByTeam_withNonExistingTag_returnsEmptyList()
            throws ProjectException {

        when(projectRepository.findByTeamContainingOrOwner(user, user))
                .thenReturn(Arrays.asList(project, secondProject));

        List<Project> result =
                projectService.getProjectsByTeam(user, null, "Python");

        assertTrue(result.isEmpty());
    }
    // 10. Update project successfully
    @Test
    void updateProject_whenValid_updatesProject() throws Exception {

        Project updatedProject = new Project();
        updatedProject.setName("Updated CollabManager");
        updatedProject.setDescription("Updated description");
        updatedProject.setTags(Arrays.asList("Java", "Docker"));

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(updatedProject, 1L);

        assertEquals("Updated CollabManager", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(Arrays.asList("Java", "Docker"), result.getTags());

        verify(projectRepository).findById(1L);
        verify(projectRepository).save(project);
    }
    // 11. Update project - project not found
    @Test
    void updateProject_whenProjectDoesNotExist_throwsException() {

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectException.class,
                () -> projectService.updateProject(new Project(), 99L)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }
    // 12. Delete project successfully
    @Test
    void deleteProject_whenUserExists_deletesProject() throws Exception {

        when(userService.findUserById(1L))
                .thenReturn(user);

        String result = projectService.deleteProject(1L, 1L);

        assertEquals("project deleted", result);

        verify(userService).findUserById(1L);
        verify(projectRepository).deleteById(1L);
    }
    // 13. Delete project - user not found
    @Test
    void deleteProject_whenUserDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(1L))
                .thenReturn(null);

        assertThrows(
                UserException.class,
                () -> projectService.deleteProject(1L, 1L)
        );

        verify(projectRepository, never()).deleteById(anyLong());
    }
    // 14. Search projects - matching projects found
    @Test
    void searchProjects_whenProjectsExist_returnsMatchingProjects()
            throws Exception {

        List<Project> projects = Arrays.asList(project);

        when(projectRepository.findByNameContainingAndTeamContains(
                "Collab", user))
                .thenReturn(projects);

        List<Project> result =
                projectService.searchProjects("Collab", user);

        assertEquals(1, result.size());
        assertSame(project, result.get(0));

        verify(projectRepository)
                .findByNameContainingAndTeamContains("Collab", user);
    }
    // 15. Search projects - no projects found
    @Test
    void searchProjects_whenNoProjectsExist_returnsEmptyList()
            throws Exception {

        when(projectRepository.findByNameContainingAndTeamContains(
                "Unknown", user))
                .thenReturn(Arrays.asList());

        List<Project> result =
                projectService.searchProjects("Unknown", user);

        assertTrue(result.isEmpty());
    }
    // 16. Add user to project successfully
    @Test
    void addUserToProject_whenUserNotInTeam_addsUser()
            throws Exception {

        Chat chat = new Chat();
        project.setChat(chat);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(userService.findUserById(2L))
                .thenReturn(user);

        projectService.addUserToProject(1L, 2L);

        assertTrue(project.getTeam().contains(user));
        assertTrue(chat.getUsers().contains(user));

        verify(projectRepository).save(project);
    }
    // 17. Add user - project not found
    @Test
    void addUserToProject_whenProjectDoesNotExist_throwsException() {

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectException.class,
                () -> projectService.addUserToProject(99L, 2L)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }
    // 18. Remove user from project successfully
    @Test
    void removeUserFromProject_whenUserExists_removesUser()
            throws Exception {

        Chat chat = new Chat();
        project.setChat(chat);

        project.getTeam().add(user);
        chat.getUsers().add(user);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(userService.findUserById(1L))
                .thenReturn(user);

        projectService.removeUserFromProject(1L, 1L);

        assertFalse(project.getTeam().contains(user));
        assertFalse(chat.getUsers().contains(user));
    }
    // 19. Remove user - project not found
    @Test
    void removeUserFromProject_whenProjectDoesNotExist_throwsException() {

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectException.class,
                () -> projectService.removeUserFromProject(99L, 1L)
        );
    }
    // 20. Get chat by project ID - success
    @Test
    void getChatByProjectId_whenProjectExists_returnsChat()
            throws Exception {

        Chat chat = new Chat();
        project.setChat(chat);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        Chat result = projectService.getChatByProjectId(1L);

        assertSame(chat, result);

        verify(projectRepository).findById(1L);
    }
    // 21. Get chat by project ID - project not found
    @Test
    void getChatByProjectId_whenProjectDoesNotExist_throwsException() {

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectException.class,
                () -> projectService.getChatByProjectId(99L)
        );
    }
    // 22. Get users by project ID - success
    @Test
    void getUsersByProjectId_whenProjectExists_returnsUsers()
            throws Exception {

        Chat chat = new Chat();
        chat.getUsers().add(user);
        project.setChat(chat);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        List<User> result = projectService.getUsersByProjectId(1L);

        assertEquals(1, result.size());
        assertTrue(result.contains(user));
    }
    // 23. Get users by project ID - project not found
    @Test
    void getUsersByProjectId_whenProjectDoesNotExist_throwsException() {

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectException.class,
                () -> projectService.getUsersByProjectId(99L)
        );
    }
}
