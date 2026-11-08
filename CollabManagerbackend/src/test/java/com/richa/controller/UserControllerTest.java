
package com.richa.controller;

import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.model.User;
import com.richa.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getUserProfileHandler_returnsUserProfileWithoutPassword()
            throws Exception {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        ResponseEntity<User> response =
                userController.getUserProfileHandler("Bearer token");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());

        // Password must not be exposed
        assertNull(response.getBody().getPassword());

        verify(userService).findUserProfileByJwt("Bearer token");
    }

    @Test
    void findUserById_returnsUserWithoutPassword()
            throws Exception {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userService.findUserById(1L))
                .thenReturn(user);

        ResponseEntity<User> response =
                userController.findUserById(1L, "Bearer token");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("test@example.com", response.getBody().getEmail());

        // Password must not be exposed
        assertNull(response.getBody().getPassword());

        verify(userService).findUserById(1L);
    }

    @Test
    void findUserById_whenUserDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserById(99L))
                .thenThrow(new UserException("User not found"));

        assertThrows(UserException.class,
                () -> userController.findUserById(99L, "Bearer token"));

        verify(userService).findUserById(99L);
    }
}
