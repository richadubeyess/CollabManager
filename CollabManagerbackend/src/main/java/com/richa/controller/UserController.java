package com.richa.controller;

import com.richa.exception.ProjectException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.richa.exception.UserException;
import com.richa.model.User;
import com.richa.service.UserService;

@RestController
@Tag(name = "Users", description = "User profile and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {


    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfileHandler(
            @RequestHeader("Authorization")@Parameter(hidden = true) String jwt) throws UserException, ProjectException {

        User user = userService.findUserProfileByJwt(jwt);
        user.setPassword(null);

        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<User> findUserById(
            @PathVariable Long userId,
            @RequestHeader("Authorization") @Parameter(hidden = true) String jwt) throws UserException {

        User user = userService.findUserById(userId);
        user.setPassword(null);

        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

}
