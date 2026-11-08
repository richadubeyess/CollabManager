
package com.richa.service;

import com.richa.exception.MailsException;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage(
                Session.getInstance(new Properties())
        );
    }

    @Test
    void sendEmailWithToken_whenEmailSentSuccessfully() throws Exception {

        when(javaMailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        String email = "test@example.com";
        String link = "http://localhost:5173/accept_invitation?token=test-token";

        emailService.sendEmailWithToken(email, link);

        assertEquals(email, mimeMessage.getRecipients(
                jakarta.mail.Message.RecipientType.TO
        )[0].toString());

        assertEquals(
                "Join Project Team Invitation",
                mimeMessage.getSubject()
        );

        assertTrue(mimeMessage.getContent().toString().contains(link));

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void sendEmailWithToken_whenSendingFails_throwsMailsException()
            throws Exception {

        when(javaMailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        doThrow(new MailSendException("SMTP error"))
                .when(javaMailSender).send(mimeMessage);

        MailsException exception = assertThrows(
                MailsException.class,
                () -> emailService.sendEmailWithToken(
                        "test@example.com",
                        "http://localhost:5173/test"
                )
        );

        assertEquals("Failed to send email", exception.getMessage());

        verify(javaMailSender).send(mimeMessage);
    }
}
