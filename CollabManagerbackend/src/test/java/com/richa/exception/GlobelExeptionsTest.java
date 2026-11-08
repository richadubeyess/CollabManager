
package com.richa.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobelExeptionsTest {

    @Mock
    private WebRequest webRequest;

    private final GlobelExeptions globelExeptions =
            new GlobelExeptions();

    @Test
    void userEceptionHandler_returnsCorrectErrorResponse() {

        UserException exception =
                new UserException("User not found");

        when(webRequest.getDescription(false))
                .thenReturn("uri=/api/users");

        LocalDateTime beforeCall = LocalDateTime.now();

        ResponseEntity<ErrorDetais> response =
                globelExeptions.userEceptionHandler(
                        exception, webRequest
                );

        LocalDateTime afterCall = LocalDateTime.now();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorDetais body = response.getBody();

        assertNotNull(body);
        assertEquals("User not found", body.getError());
        assertEquals("uri=/api/users", body.getMessage());

        assertNotNull(body.getTimestamp());
        assertFalse(body.getTimestamp().isBefore(beforeCall));
        assertFalse(body.getTimestamp().isAfter(afterCall));

        verify(webRequest).getDescription(false);
    }

    @Test
    void otherEceptionHandler_returnsCorrectErrorResponse() {

        Exception exception =
                new RuntimeException("Something went wrong");

        when(webRequest.getDescription(false))
                .thenReturn("uri=/api/test");

        LocalDateTime beforeCall = LocalDateTime.now();

        ResponseEntity<ErrorDetais> response =
                globelExeptions.otherEceptionHandler(
                        exception, webRequest
                );

        LocalDateTime afterCall = LocalDateTime.now();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorDetais body = response.getBody();

        assertNotNull(body);
        assertEquals("Something went wrong", body.getError());
        assertEquals("uri=/api/test", body.getMessage());

        assertNotNull(body.getTimestamp());
        assertFalse(body.getTimestamp().isBefore(beforeCall));
        assertFalse(body.getTimestamp().isAfter(afterCall));

        verify(webRequest).getDescription(false);
    }
}
