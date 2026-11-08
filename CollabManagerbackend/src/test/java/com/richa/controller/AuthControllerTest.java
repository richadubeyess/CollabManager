
package com.richa.controller;

import com.richa.config.JwtProvider;
import com.richa.exception.UserException;
import com.richa.model.Subscription;
import com.richa.model.User;
import com.richa.repository.SubscriptionRepository;
import com.richa.repository.UserRepository;
import com.richa.request.LoginRequest;
import com.richa.response.AuthResponse;
import com.richa.service.CustomeUserServiceImplementation;
import com.richa.service.CustomeUserServiceImplementation;
import com.richa.service.SubscriptionService;
import com.richa.service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomeUserServiceImplementation customUserDetails;

    @Mock
    private UserService userService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private MockedStatic<JwtProvider> jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = mockStatic(JwtProvider.class);
        jwtProvider.when(() -> JwtProvider.generateToken(any(Authentication.class)))
                .thenReturn("test-jwt-token");
    }

    @AfterEach
    void tearDown() {
        jwtProvider.close();
        SecurityContextHolder.clearContext();
    }

    // 1. Signup successful
    @Test
    void createUserHandler_whenValidUser_returnsSuccess() throws Exception {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");
        user.setRole("CUSTOMER");

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));
        when(subscriptionService.createSubscription(any(User.class)))
                .thenReturn(new Subscription());

        var response = authController.createUserHandler(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test-jwt-token", response.getBody().getJwt());
        assertEquals("Register Success", response.getBody().getMessage());

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
        verify(subscriptionService).createSubscription(any(User.class));
    }

    // 2. Signup with existing email
    @Test
    void createUserHandler_whenEmailAlreadyExists_throwsException() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(new User());

        assertThrows(UserException.class,
                () -> authController.createUserHandler(user));

        verify(userRepository, never()).save(any(User.class));
        verify(subscriptionService, never())
                .createSubscription(any(User.class));
    }

    // 3. Signin successful
    @Test
    void signin_whenValidCredentials_returnsSuccess() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        "test@example.com",
                        "encoded-password",
                        List.of()
                );

        when(customUserDetails.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);

        var response = authController.signin(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test-jwt-token", response.getBody().getJwt());
        assertEquals("Login Success", response.getBody().getMessage());

        verify(customUserDetails).loadUserByUsername("test@example.com");
        verify(passwordEncoder).matches("password123", "encoded-password");
    }

    // 4. Signin with incorrect password
    @Test
    void signin_whenPasswordIsIncorrect_throwsException() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrong-password");

        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        "test@example.com",
                        "encoded-password",
                        List.of()
                );

        when(customUserDetails.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authController.signin(loginRequest));

        jwtProvider.verifyNoInteractions();
    }
}
