
package com.richa.service;

import com.richa.exception.MailsException;
import com.richa.model.Invitation;
import com.richa.repository.InviteTokenRepository;
import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteTokenServiceImplTest {

    @Mock
    private InviteTokenRepository invitationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InviteTokenServiceImpl invitationService;

    private Invitation invitation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                invitationService, "frontendUrl", "http://localhost:5173"
        );

        invitation = new Invitation();
        invitation.setEmail("test@example.com");
        invitation.setProjectId(10L);
        invitation.setToken("test-token");
    }

    // 1. Send invitation successfully
    @Test
    void sendInvitation_savesInvitationAndSendsEmail()
            throws MailsException, MessagingException {

        invitationService.sendInvitation("test@example.com", 10L);

        ArgumentCaptor<Invitation> captor =
                ArgumentCaptor.forClass(Invitation.class);

        verify(invitationRepository).save(captor.capture());

        Invitation savedInvitation = captor.getValue();

        assertEquals("test@example.com", savedInvitation.getEmail());
        assertEquals(10L, savedInvitation.getProjectId());
        assertNotNull(savedInvitation.getToken());

        assertDoesNotThrow(() ->
                UUID.fromString(savedInvitation.getToken())
        );

        verify(emailService).sendEmailWithToken(
                eq("test@example.com"),
                eq("http://localhost:5173/accept_invitation?token="
                        + savedInvitation.getToken())
        );
    }

    // 2. Accept invitation with valid token
    @Test
    void acceptInvitation_whenTokenExists_returnsInvitation()
            throws Exception {

        when(invitationRepository.findByToken("test-token"))
                .thenReturn(invitation);

        Invitation result =
                invitationService.acceptInvitation("test-token", 1L);

        assertNotNull(result);
        assertEquals(invitation, result);

        verify(invitationRepository).findByToken("test-token");
    }

    // 3. Accept invitation with invalid token
    @Test
    void acceptInvitation_whenTokenDoesNotExist_throwsException() {

        when(invitationRepository.findByToken("invalid-token"))
                .thenReturn(null);

        Exception exception = assertThrows(
                Exception.class,
                () -> invitationService.acceptInvitation("invalid-token", 1L)
        );

        assertEquals("Invalid invitation token", exception.getMessage());

        verify(invitationRepository).findByToken("invalid-token");
    }

    // 4. Get token by user email
    @Test
    void getTokenByUserMail_returnsToken() {

        when(invitationRepository.findByEmail("test@example.com"))
                .thenReturn(invitation);

        String result =
                invitationService.getTokenByUserMail("test@example.com");

        assertEquals("test-token", result);

        verify(invitationRepository).findByEmail("test@example.com");
    }

    // 5. Delete invitation token
    @Test
    void deleteToken_deletesToken() {

        invitationService.deleteToken("test-token");

        verify(invitationRepository).deleteByToken("test-token");
    }
}
