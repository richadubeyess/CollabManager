
package com.richa.controller;

import com.richa.DTO.IssueDTO;
import com.richa.exception.IssueException;
import com.richa.model.Issue;
import com.richa.model.User;
import com.richa.request.IssueRequest;
import com.richa.response.AuthResponse;
import com.richa.service.IssueService;
import com.richa.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueControllerTest {

    @Mock
    private IssueService issueService;

    @Mock
    private UserService userService;

    @InjectMocks
    private IssueController issueController;

    @Test
    void getIssueById_returnsIssue() throws Exception {

        Issue issue = mock(Issue.class);

        when(issueService.getIssueById(1L))
                .thenReturn(Optional.of(issue));

        ResponseEntity<Issue> response =
                issueController.getIssueById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(issue, response.getBody());

        verify(issueService).getIssueById(1L);
    }

    @Test
    void getIssueByProjectId_returnsIssues() throws Exception {

        List<Issue> issues = List.of(mock(Issue.class));

        when(issueService.getIssueByProjectId(10L))
                .thenReturn(issues);

        ResponseEntity<List<Issue>> response =
                issueController.getIssueByProjectId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(issues, response.getBody());

        verify(issueService).getIssueByProjectId(10L);
    }

    @Test
    void createIssue_returnsIssueDTO() throws Exception {

        String token = "Bearer test-token";

        User tokenUser = mock(User.class);
        User user = mock(User.class);
        IssueRequest request = mock(IssueRequest.class);
        Issue createdIssue = mock(Issue.class);

        when(tokenUser.getId()).thenReturn(1L);

        when(userService.findUserProfileByJwt(token))
                .thenReturn(tokenUser);

        when(userService.findUserById(1L))
                .thenReturn(user);

        when(issueService.createIssue(request, 1L))
                .thenReturn(createdIssue);

        ResponseEntity<IssueDTO> response =
                issueController.createIssue(request, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(issueService).createIssue(request, 1L);
    }

    @Test
    void createIssue_whenUserDoesNotExist_returnsUnauthorized()
            throws Exception {

        String token = "Bearer test-token";

        User tokenUser = mock(User.class);
        IssueRequest request = mock(IssueRequest.class);

        when(tokenUser.getId()).thenReturn(1L);

        when(userService.findUserProfileByJwt(token))
                .thenReturn(tokenUser);

        when(userService.findUserById(1L))
                .thenReturn(null);

        ResponseEntity<IssueDTO> response =
                issueController.createIssue(request, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(issueService, never()).createIssue(any(), any());
    }

    @Test
    void updateIssue_returnsUpdatedIssue() throws Exception {

        String token = "Bearer test-token";

        User user = mock(User.class);
        IssueRequest request = mock(IssueRequest.class);
        Issue updatedIssue = mock(Issue.class);

        when(userService.findUserProfileByJwt(token))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(issueService.updateIssue(5L, request, 1L))
                .thenReturn(Optional.of(updatedIssue));

        ResponseEntity<Issue> response =
                issueController.updateIssue(5L, request, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updatedIssue, response.getBody());

        verify(issueService).updateIssue(5L, request, 1L);
    }

    @Test
    void deleteIssue_returnsSuccessResponse() throws Exception {

        String token = "Bearer test-token";

        User user = mock(User.class);

        when(userService.findUserProfileByJwt(token))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(issueService.deleteIssue(5L, 1L))
                .thenReturn("deleted");

        ResponseEntity<AuthResponse> response =
                issueController.deleteIssue(5L, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Issue deleted", response.getBody().getMessage());
        assertTrue(response.getBody().isStatus());

        verify(issueService).deleteIssue(5L, 1L);
    }

    @Test
    void searchIssues_returnsFilteredIssues() throws Exception {

        List<Issue> issues = List.of(mock(Issue.class));

        when(issueService.searchIssues(
                "Bug", "OPEN", "HIGH", 2L))
                .thenReturn(issues);

        ResponseEntity<List<Issue>> response =
                issueController.searchIssues(
                        "Bug", "OPEN", "HIGH", 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(issues, response.getBody());

        verify(issueService).searchIssues(
                "Bug", "OPEN", "HIGH", 2L);
    }

    @Test
    void addUserToIssue_returnsUpdatedIssue() throws Exception {

        Issue issue = mock(Issue.class);

        when(issueService.addUserToIssue(5L, 2L))
                .thenReturn(issue);

        ResponseEntity<Issue> response =
                issueController.addUserToIssue(5L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(issue, response.getBody());

        verify(issueService).addUserToIssue(5L, 2L);
    }

    @Test
    void getIssuesByAssigneeId_returnsIssues() throws Exception {

        List<Issue> issues = List.of(mock(Issue.class));

        when(issueService.getIssuesByAssigneeId(2L))
                .thenReturn(issues);

        ResponseEntity<List<Issue>> response =
                issueController.getIssuesByAssigneeId(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(issues, response.getBody());

        verify(issueService).getIssuesByAssigneeId(2L);
    }

    @Test
    void updateIssueStatus_returnsUpdatedIssue() throws Exception {

        Issue issue = mock(Issue.class);

        when(issueService.updateStatus(5L, "COMPLETED"))
                .thenReturn(issue);

        ResponseEntity<Issue> response =
                issueController.updateIssueStatus(
                        "COMPLETED", 5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(issue, response.getBody());

        verify(issueService).updateStatus(5L, "COMPLETED");
    }
}
