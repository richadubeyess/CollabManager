
package com.richa.controller;

import com.richa.model.Comment;
import com.richa.model.User;
import com.richa.request.CreateCommentRequest;
import com.richa.response.MessageResponse;
import com.richa.service.CommentService;
import com.richa.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentService commentService;

    @Mock
    private UserService userService;

    // 1. Create comment successfully
    @Test
    void createComment_whenValidRequest_returnsCreatedComment() throws Exception {

        String jwt = "test-jwt-token";

        User user = new User();
        user.setId(1L);

        CreateCommentRequest request = new CreateCommentRequest();
        request.setIssueId(10L);
        request.setContent("This is a test comment");

        Comment comment = new Comment();
        comment.setId(100L);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);

        when(commentService.createComment(
                10L, 1L, "This is a test comment"
        )).thenReturn(comment);

        ResponseEntity<Comment> response =
                commentController.createComment(request, jwt);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getId());

        verify(userService).findUserProfileByJwt(jwt);
        verify(commentService).createComment(
                10L, 1L, "This is a test comment"
        );
    }

    // 2. Delete comment successfully
    @Test
    void deleteComment_whenValidRequest_returnsSuccessMessage() throws Exception {

        String jwt = "test-jwt-token";

        User user = new User();
        user.setId(1L);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);

        ResponseEntity<MessageResponse> response =
                commentController.deleteComment(100L, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "comment deleted successfully",
                response.getBody().getMessage()
        );

        verify(userService).findUserProfileByJwt(jwt);
        verify(commentService).deleteComment(100L, 1L);
    }

    // 3. Get comments by issue ID
    @Test
    void getCommentsByIssueId_whenCommentsExist_returnsComments() {

        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(2L);

        List<Comment> comments = List.of(comment1, comment2);

        when(commentService.findCommentByIssueId(10L))
                .thenReturn(comments);

        ResponseEntity<List<Comment>> response =
                commentController.getCommentsByIssueId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(commentService).findCommentByIssueId(10L);
    }
}
