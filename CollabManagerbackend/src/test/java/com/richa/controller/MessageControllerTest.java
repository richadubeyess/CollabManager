
package com.richa.controller;

import com.richa.exception.ChatException;
import com.richa.exception.UserException;
import com.richa.model.Chat;
import com.richa.model.Message;
import com.richa.model.Project;
import com.richa.model.User;
import com.richa.request.CreateMessageRequest;
import com.richa.service.MessageService;
import com.richa.service.ProjectService;
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
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private MessageController messageController;

    @Test
    void sendMessage_success() throws Exception {

        CreateMessageRequest request = mock(CreateMessageRequest.class);
        User user = mock(User.class);
        Project project = mock(Project.class);
        Chat chat = mock(Chat.class);
        Message message = mock(Message.class);

        when(request.getSenderId()).thenReturn(1L);
        when(request.getProjectId()).thenReturn(10L);
        when(request.getContent()).thenReturn("Hello team!");

        when(userService.findUserById(1L)).thenReturn(user);
        when(projectService.getProjectById(10L)).thenReturn(project);
        when(project.getChat()).thenReturn(chat);

        when(messageService.sendMessage(1L, 10L, "Hello team!"))
                .thenReturn(message);

        ResponseEntity<Message> response =
                messageController.sendMessage(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(message, response.getBody());

        verify(messageService).sendMessage(1L, 10L, "Hello team!");
    }

    @Test
    void sendMessage_whenUserDoesNotExist_throwsUserException()
            throws Exception {

        CreateMessageRequest request = mock(CreateMessageRequest.class);

        when(request.getSenderId()).thenReturn(1L);
        when(userService.findUserById(1L)).thenReturn(null);

        assertThrows(UserException.class,
                () -> messageController.sendMessage(request));

        verify(projectService, never()).getProjectById(anyLong());
        verify(messageService, never())
                .sendMessage(anyLong(), anyLong(), anyString());
    }

    @Test
    void sendMessage_whenChatDoesNotExist_throwsChatException()
            throws Exception {

        CreateMessageRequest request = mock(CreateMessageRequest.class);
        User user = mock(User.class);
        Project project = mock(Project.class);

        when(request.getSenderId()).thenReturn(1L);
        when(request.getProjectId()).thenReturn(10L);

        when(userService.findUserById(1L)).thenReturn(user);
        when(projectService.getProjectById(10L)).thenReturn(project);
        when(project.getChat()).thenReturn(null);

        assertThrows(ChatException.class,
                () -> messageController.sendMessage(request));

        verify(messageService, never())
                .sendMessage(anyLong(), anyLong(), anyString());
    }

    @Test
    void getMessagesByChatId_returnsMessages() throws Exception {

        List<Message> messages = List.of(
                mock(Message.class),
                mock(Message.class)
        );

        when(messageService.getMessagesByProjectId(10L))
                .thenReturn(messages);

        ResponseEntity<List<Message>> response =
                messageController.getMessagesByChatId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(messages, response.getBody());

        verify(messageService).getMessagesByProjectId(10L);
    }
}
