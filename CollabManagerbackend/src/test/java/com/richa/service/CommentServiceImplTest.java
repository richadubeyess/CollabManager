
package com.richa.service;

import com.richa.exception.IssueException;
import com.richa.exception.UserException;
import com.richa.model.Comment;
import com.richa.model.Issue;
import com.richa.model.User;
import com.richa.repository.CommentRepository;
import com.richa.repository.IssueRepository;
import com.richa.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private User anotherUser;
    private Issue issue;
    private Comment comment;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@example.com");

        issue = new Issue();
        issue.setId(100L);
        issue.setTitle("Fix login bug");

        // Ensure the issue has an initialized comments collection.
        issue.setComments(new ArrayList<>());

        comment = new Comment();
        comment.setId(10L);
        comment.setContent("This is a test comment");
        comment.setUser(user);
        comment.setIssue(issue);
        comment.setCreatedDateTime(LocalDateTime.now());
    }

    // 1. Create comment successfully
    @Test
    void createComment_whenValidData_returnsSavedComment() throws Exception {

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        Comment result = commentService.createComment(
                100L, 1L, "This is a test comment"
        );

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("This is a test comment", result.getContent());
        assertEquals(user, result.getUser());
        assertEquals(issue, result.getIssue());
        assertNotNull(result.getCreatedDateTime());

        assertTrue(issue.getComments().contains(result));

        verify(commentRepository).save(any(Comment.class));
    }

    // 2. Create comment - issue not found
    @Test
    void createComment_whenIssueDoesNotExist_throwsException() {

        when(issueRepository.findById(10L))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IssueException.class,
                () -> commentService.createComment(10L, 1L, "Test comment")
        );

        verify(issueRepository).findById(10L);
        verify(userRepository).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // 3. Create comment - user not found
    @Test
    void createComment_whenUserDoesNotExist_throwsException() {

        when(issueRepository.findById(100L))
                .thenReturn(Optional.of(issue));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserException.class,
                () -> commentService.createComment(
                        100L, 1L, "Test comment"
                )
        );

        verify(commentRepository, never()).save(any());
    }

    // 4. Delete comment successfully by its owner
    @Test
    void deleteComment_whenUserIsOwner_deletesComment() throws Exception {

        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(comment));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        commentService.deleteComment(10L, 1L);

        verify(commentRepository).delete(comment);
    }

    // 5. Delete comment - comment not found
    @Test
    void deleteComment_whenCommentDoesNotExist_throwsException() {

        when(commentRepository.findById(10L))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IssueException.class,
                () -> commentService.deleteComment(10L, 1L)
        );

        verify(commentRepository).findById(10L);
        verify(userRepository).findById(1L);
        verify(commentRepository, never()).delete(any());
    }
    // 6. Delete comment - user not found
    @Test
    void deleteComment_whenUserDoesNotExist_throwsException() {

        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(comment));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserException.class,
                () -> commentService.deleteComment(10L, 1L)
        );

        verify(commentRepository, never()).delete(any());
    }

    // 7. Delete comment - user is not the owner
    @Test
    void deleteComment_whenUserIsNotOwner_throwsException() {

        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(comment));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(anotherUser));

        assertThrows(
                UserException.class,
                () -> commentService.deleteComment(10L, 2L)
        );

        verify(commentRepository, never()).delete(any());
    }

    // 8. Find comments by issue ID
    @Test
    void findCommentByIssueId_whenCommentsExist_returnsComments() {

        List<Comment> comments = List.of(comment);

        when(commentRepository.findByIssueId(100L))
                .thenReturn(comments);

        List<Comment> result =
                commentService.findCommentByIssueId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("This is a test comment", result.get(0).getContent());

        verify(commentRepository).findByIssueId(100L);
    }

    // 9. Find comments by issue ID - no comments
    @Test
    void findCommentByIssueId_whenNoCommentsExist_returnsEmptyList() {

        when(commentRepository.findByIssueId(100L))
                .thenReturn(new ArrayList<>());

        List<Comment> result =
                commentService.findCommentByIssueId(100L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
