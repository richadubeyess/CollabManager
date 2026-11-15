package com.richa.controller;

import com.richa.exception.ChatException;
import com.richa.exception.UserException;
import com.richa.model.Message;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Real-time Chat", description = "WebSocket messaging endpoints")
public class RealTimeChatController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/chat/{groupId}")
    public Message sendToUser(@Payload Message message,

                              @DestinationVariable String groupId) throws UserException, ChatException {


        simpMessagingTemplate.convertAndSendToUser(groupId, "/private",
                message);



        return message;
    }



}
