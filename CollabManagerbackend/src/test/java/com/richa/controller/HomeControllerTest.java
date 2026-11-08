
package com.richa.controller;

import com.richa.response.ApiResponse;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    private final HomeController homeController = new HomeController();

    @Test
    void homeController_returnsWelcomeMessage() {

        ResponseEntity<ApiResponse> response =
                homeController.homeController();

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("welcome to user service",
                response.getBody().getMessage());
        assertTrue(response.getBody().isStatus());
    }
}
