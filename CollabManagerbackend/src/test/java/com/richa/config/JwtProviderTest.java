
package com.richa.config;

import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    @Test
    void generateToken_generatesValidJwt() {

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(
                        "test@example.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        String token = JwtProvider.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify that the generated token contains the correct email
        assertEquals(
                "test@example.com",
                JwtProvider.getEmailFromJwtToken("Bearer " + token)
        );
    }

    @Test
    void getEmailFromJwtToken_returnsCorrectEmail() {

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(
                        "user@gmail.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        String token = JwtProvider.generateToken(auth);

        String email =
                JwtProvider.getEmailFromJwtToken("Bearer " + token);

        assertEquals("user@gmail.com", email);
    }

    @Test
    void getEmailFromJwtToken_withInvalidToken_throwsException() {

        String invalidToken = "Bearer invalid.jwt.token";

        assertThrows(
                JwtException.class,
                () -> JwtProvider.getEmailFromJwtToken(invalidToken)
        );
    }

    @Test
    void populateAuthorities_returnsCommaSeparatedAuthorities() {

        List<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        String result = JwtProvider.populateAuthorities(authorities);

        Set<String> actual = Set.of(result.split(","));

        assertEquals(
                Set.of("ROLE_USER", "ROLE_ADMIN"),
                actual
        );
    }

    @Test
    void populateAuthorities_withEmptyCollection_returnsEmptyString() {

        String result = JwtProvider.populateAuthorities(List.of());

        assertEquals("", result);
    }
}
