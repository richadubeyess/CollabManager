
package com.richa.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenValidatorTest {

    private final JwtTokenValidator jwtTokenValidator =
            new JwtTokenValidator();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_setsAuthentication()
            throws Exception {

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(
                        "test@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        String token = JwtProvider.generateToken(auth);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER,
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();

        jwtTokenValidator.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("test@example.com", authentication.getName());
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void doFilterInternal_withoutToken_doesNotSetAuthentication()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();

        jwtTokenValidator.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void doFilterInternal_withInvalidToken_throwsException()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER,
                "Bearer invalid.jwt.token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();

        assertThrows(
                BadCredentialsException.class,
                () -> jwtTokenValidator.doFilter(
                        request, response, filterChain
                )
        );
    }

    @Test
    void doFilterInternal_withValidToken_setsCorrectAuthorities()
            throws Exception {

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(
                        "admin@example.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        String token = JwtProvider.generateToken(auth);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER,
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();

        jwtTokenValidator.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("admin@example.com", authentication.getName());

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
