
package com.richa.service;

import com.richa.exception.UserException;
import com.richa.model.Chat;
import com.richa.model.Message;
import com.richa.model.Project;
import com.richa.model.User;
import com.richa.repository.MessageRepository;
import com.richa.repository.UserRepository;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User user;
    private Chat chat;
    private Project project;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        chat = new Chat();
        chat.setId(10L);
        chat.setMessages(new ArrayList<>());

        project = new Project();
        project.setId(100L);
        project.setChat(chat);
    }

    @Test
    void sendMessage_whenValidDetails_returnsSavedMessage() throws Exception {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectService.getProjectById(100L)).thenReturn(project);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = messageService.sendMessage(
                1L, 100L, "Hello team!"
        );

        assertNotNull(result);
        assertEquals("Hello team!", result.getContent());
        assertEquals(user, result.getSender());
        assertEquals(chat, result.getChat());
        assertNotNull(result.getCreatedAt());

        assertTrue(chat.getMessages().contains(result));

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_whenSenderDoesNotExist_throwsUserException() throws Exception {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserException.class,
                () -> messageService.sendMessage(1L, 100L, "Hello")
        );

        verify(messageRepository, never()).save(any(Message.class));
        verify(projectService, never()).getProjectById(anyLong());
    }

    @Test
    void getMessagesByProjectId_whenMessagesExist_returnsMessages()
            throws Exception {

        List<Message> messages = new ArrayList<>();

        Message message = new Message();
        message.setContent("Hello team!");

        messages.add(message);

        when(projectService.getChatByProjectId(100L)).thenReturn(chat);
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(10L))
                .thenReturn(messages);

        List<Message> result =
                messageService.getMessagesByProjectId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello team!", result.get(0).getContent());

        verify(messageRepository)
                .findByChatIdOrderByCreatedAtAsc(10L);
    }

    @Test
    void getMessagesByProjectId_whenNoMessages_returnsEmptyList()
            throws Exception {

        when(projectService.getChatByProjectId(100L)).thenReturn(chat);
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(10L))
                .thenReturn(new ArrayList<>());

        List<Message> result =
                messageService.getMessagesByProjectId(100L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
