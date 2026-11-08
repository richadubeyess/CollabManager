
package com.richa.service;

import com.richa.exception.IssueException;
import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.model.Issue;
import com.richa.model.Project;
import com.richa.model.User;
import com.richa.repository.IssueRepository;
import com.richa.request.IssueRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @Mock
    private NotificationServiceImpl notificationServiceImpl;

    @InjectMocks
    private IssueServiceImpl issueService;

    private User user;
    private Project project;
    private Issue issue;
    private IssueRequest issueRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        project = new Project();
        project.setId(10L);
        project.setName("CollabManager");

        issue = new Issue();
        issue.setId(100L);
        issue.setTitle("Fix login bug");
        issue.setDescription("Fix authentication issue");
        issue.setStatus("PENDING");
        issue.setPriority("HIGH");
        issue.setProject(project);
        issue.setAssignee(user);

        issueRequest = new IssueRequest();
        issueRequest.setTitle("Fix login bug");
        issueRequest.setDescription("Fix authentication issue");
        issueRequest.setStatus("PENDING");
        issueRequest.setPriority("HIGH");
        issueRequest.setProjectId(10L);
    }

    // 1. Get issue by ID - success
    @Test
    void getIssueById_whenIssueExists_returnsIssue() throws Exception {

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        Optional<Issue> result = issueService.getIssueById(100L);

        assertTrue(result.isPresent());
        assertEquals("Fix login bug", result.get().getTitle());

        verify(issueRepository).findById(100L);
    }

    // 2. Get issue by ID - not found
    @Test
    void getIssueById_whenIssueDoesNotExist_throwsException() {

        when(issueRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IssueException.class,
                () -> issueService.getIssueById(100L)
        );
    }

    // 3. Get issues by project ID
    @Test
    void getIssueByProjectId_returnsIssues() throws Exception {

        List<Issue> issues = List.of(issue);

        when(projectService.getProjectById(10L))
                .thenReturn(project);

        when(issueRepository.findByProjectId(10L))
                .thenReturn(issues);

        List<Issue> result = issueService.getIssueByProjectId(10L);

        assertEquals(1, result.size());
        assertEquals("Fix login bug", result.get(0).getTitle());

        verify(projectService).getProjectById(10L);
        verify(issueRepository).findByProjectId(10L);
    }

    // 4. Get issues by project ID - project not found
    @Test
    void getIssueByProjectId_whenProjectDoesNotExist_throwsException()
            throws Exception {

        when(projectService.getProjectById(10L))
                .thenThrow(new ProjectException("Project not found"));

        assertThrows(
                ProjectException.class,
                () -> issueService.getIssueByProjectId(10L)
        );

        verify(issueRepository, never()).findByProjectId(anyLong());
    }

    // 5. Create issue successfully
    @Test
    void createIssue_whenValidData_returnsCreatedIssue() throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);
        when(projectService.getProjectById(10L)).thenReturn(project);

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(issueRequest, 1L);

        assertNotNull(result);
        assertEquals("Fix login bug", result.getTitle());
        assertEquals("Fix authentication issue", result.getDescription());
        assertEquals("PENDING", result.getStatus());
        assertEquals("HIGH", result.getPriority());
        assertEquals(project, result.getProject());

        verify(issueRepository).save(any(Issue.class));
    }

    // 6. Create issue - user not found
    @Test
    void createIssue_whenUserDoesNotExist_throwsException() throws Exception {

        when(userService.findUserById(1L)).thenReturn(null);

        assertThrows(
                UserException.class,
                () -> issueService.createIssue(issueRequest, 1L)
        );

        verify(issueRepository, never()).save(any());
    }

    // 7. Create issue - project not found
    @Test
    void createIssue_whenProjectDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);

        when(projectService.getProjectById(10L))
                .thenThrow(new ProjectException("Project not found"));

        assertThrows(
                ProjectException.class,
                () -> issueService.createIssue(issueRequest, 1L)
        );

        verify(issueRepository, never()).save(any());
    }

    // 8. Update issue successfully
    @Test
    void updateIssue_whenValidData_updatesIssue() throws Exception {

        IssueRequest updatedRequest = new IssueRequest();
        updatedRequest.setTitle("Updated issue");
        updatedRequest.setDescription("Updated description");
        updatedRequest.setStatus("IN_PROGRESS");
        updatedRequest.setPriority("MEDIUM");

        when(userService.findUserById(1L)).thenReturn(user);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Issue> result =
                issueService.updateIssue(100L, updatedRequest, 1L);

        assertTrue(result.isPresent());
        assertEquals("Updated issue", result.get().getTitle());
        assertEquals("Updated description", result.get().getDescription());
        assertEquals("IN_PROGRESS", result.get().getStatus());
        assertEquals("MEDIUM", result.get().getPriority());

        verify(issueRepository).save(issue);
    }

    // 9. Update issue - issue not found
    @Test
    void updateIssue_whenIssueDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IssueException.class,
                () -> issueService.updateIssue(100L, issueRequest, 1L)
        );

        verify(issueRepository, never()).save(any());
    }

    // 10. Update issue - user not found
    @Test
    void updateIssue_whenUserDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(1L)).thenReturn(null);

        assertThrows(
                UserException.class,
                () -> issueService.updateIssue(100L, issueRequest, 1L)
        );
    }

    // 11. Delete issue successfully
    @Test
    void deleteIssue_whenIssueExists_deletesIssue() throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        doNothing().when(issueRepository).deleteById(100L);

        String result = issueService.deleteIssue(100L, 1L);

        assertEquals("issue with the id100deleted", result);

        verify(issueRepository).deleteById(100L);
    }

    // 12. Delete issue - issue not found
    @Test
    void deleteIssue_whenIssueDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IssueException.class,
                () -> issueService.deleteIssue(100L, 1L)
        );

        verify(issueRepository, never()).deleteById(anyLong());
    }

    // 13. Get issues by assignee ID
    @Test
    void getIssuesByAssigneeId_returnsIssues() throws Exception {

        when(issueRepository.findByAssigneeId(1L))
                .thenReturn(List.of(issue));

        List<Issue> result = issueService.getIssuesByAssigneeId(1L);

        assertEquals(1, result.size());
        assertEquals(issue, result.get(0));
    }

    // 14. Get issues by assignee ID - empty list
    @Test
    void getIssuesByAssigneeId_whenNoIssues_returnsEmptyList()
            throws Exception {

        when(issueRepository.findByAssigneeId(1L))
                .thenReturn(new ArrayList<>());

        List<Issue> result = issueService.getIssuesByAssigneeId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 15. Get issues by assignee ID - null result
    @Test
    void getIssuesByAssigneeId_whenResultIsNull_throwsException() {

        when(issueRepository.findByAssigneeId(1L)).thenReturn(null);

        assertThrows(
                IssueException.class,
                () -> issueService.getIssuesByAssigneeId(1L)
        );
    }

    // 16. Search issues successfully
    @Test
    void searchIssues_whenMatchingIssuesExist_returnsIssues()
            throws Exception {

        when(issueRepository.searchIssues(
                "login", "PENDING", "HIGH", 1L
        )).thenReturn(List.of(issue));

        List<Issue> result = issueService.searchIssues(
                "login", "PENDING", "HIGH", 1L
        );

        assertEquals(1, result.size());
        assertEquals("Fix login bug", result.get(0).getTitle());
    }

    // 17. Search issues - empty result
    @Test
    void searchIssues_whenNoIssuesMatch_returnsEmptyList()
            throws Exception {

        when(issueRepository.searchIssues(
                "unknown", "PENDING", "HIGH", 1L
        )).thenReturn(new ArrayList<>());

        List<Issue> result = issueService.searchIssues(
                "unknown", "PENDING", "HIGH", 1L
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 18. Search issues - null result
    @Test
    void searchIssues_whenResultIsNull_throwsException() {

        when(issueRepository.searchIssues(
                "login", "PENDING", "HIGH", 1L
        )).thenReturn(null);

        assertThrows(
                IssueException.class,
                () -> issueService.searchIssues(
                        "login", "PENDING", "HIGH", 1L
                )
        );
    }

    // 19. Get assignees for issue
    @Test
    void getAssigneeForIssue_currentImplementationReturnsNull()
            throws Exception {

        assertNull(issueService.getAssigneeForIssue(100L));
    }

    // 20. Add user to issue successfully
    @Test
    void addUserToIssue_whenValidData_assignsUserAndSendsNotification()
            throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.addUserToIssue(100L, 1L);

        assertNotNull(result);
        assertEquals(user, result.getAssignee());

        verify(notificationServiceImpl).sendNotification(
                "test@example.com",
                "New Issue Assigned To You",
                "New Issue Assign To You"
        );

        verify(issueRepository).save(issue);
    }

    // 21. Add user to issue - issue not found
    @Test
    void addUserToIssue_whenIssueDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(1L)).thenReturn(user);

        when(issueRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IssueException.class,
                () -> issueService.addUserToIssue(100L, 1L)
        );

        verify(issueRepository, never()).save(any());
        verify(notificationServiceImpl, never())
                .sendNotification(anyString(), anyString(), anyString());
    }

    // 22. Update issue status successfully
    @Test
    void updateStatus_whenIssueExists_updatesStatus() throws Exception {

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.updateStatus(100L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());

        verify(issueRepository).save(issue);
    }

    // 23. Update issue status - issue not found
    @Test
    void updateStatus_whenIssueDoesNotExist_throwsException() {

        when(issueRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                IssueException.class,
                () -> issueService.updateStatus(100L, "COMPLETED")
        );

        verify(issueRepository, never()).save(any());
    }
}
