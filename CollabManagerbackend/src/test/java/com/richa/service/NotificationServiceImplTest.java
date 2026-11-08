
package com.richa.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void sendNotification_whenValidDetails_sendsEmail() {

        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "This is a test notification";

        notificationService.sendNotification(to, subject, body);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertArrayEquals(new String[]{to}, sentMessage.getTo());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }

    @Test
    void sendNotification_whenMailSendingFails_throwsException() {

        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test body";

        doThrow(new MailSendException("Mail sending failed"))
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(
                MailSendException.class,
                () -> notificationService.sendNotification(to, subject, body)
        );

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}
