
package com.richa.service;

import com.richa.model.Chat;
import com.richa.repository.ChatRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat();
    }

    @Test
    void createChat_whenChatIsValid_savesAndReturnsChat() {

        when(chatRepository.save(chat)).thenReturn(chat);

        Chat result = chatService.createChat(chat);

        assertSame(chat, result);
        verify(chatRepository).save(chat);
    }

    @Test
    void createChat_returnsRepositorySavedChat() {

        Chat savedChat = new Chat();

        when(chatRepository.save(chat)).thenReturn(savedChat);

        Chat result = chatService.createChat(chat);

        assertSame(savedChat, result);
        verify(chatRepository).save(chat);
    }
}

