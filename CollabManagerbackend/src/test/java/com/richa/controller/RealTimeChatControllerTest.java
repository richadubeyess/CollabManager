
package com.richa.controller;

import com.richa.model.Message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealTimeChatControllerTest {

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private RealTimeChatController realTimeChatController;

    @Test
    void sendToUser_sendsMessageToCorrectDestination() throws Exception {

        Message message = new Message();
        message.setContent("Hello team!");

        String groupId = "group123";

        Message response =
                realTimeChatController.sendToUser(message, groupId);

        // Verify returned message
        assertNotNull(response);
        assertEquals(message, response);

        // Verify message was sent to the correct destination
        verify(simpMessagingTemplate, times(1))
                .convertAndSendToUser(
                        groupId,
                        "/private",
                        message
                );
    }
}
