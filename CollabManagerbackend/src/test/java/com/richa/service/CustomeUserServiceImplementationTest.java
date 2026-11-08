
package com.richa.service;

import com.richa.model.User;
import com.richa.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomeUserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomeUserServiceImplementation customUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("ADMIN");
    }

    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetails() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(user);

        UserDetails result =
                customUserService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN")));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_whenRoleIsNull_defaultsToCustomer() {

        user.setRole(null);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(user);

        UserDetails result =
                customUserService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("CUSTOMER")));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_throwsException() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserService.loadUserByUsername("unknown@example.com")
        );

        assertTrue(exception.getMessage().contains("unknown@example.com"));

        verify(userRepository).findByEmail("unknown@example.com");
    }
}
