package com.richa.service;

import java.util.List;

import com.richa.exception.ChatException;
import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.model.Message;

public interface MessageService {

    Message sendMessage(Long senderId, Long chatId, String content) throws UserException, ChatException, ProjectException;

    List<Message> getMessagesByProjectId(Long projectId) throws ProjectException, ChatException;
}

