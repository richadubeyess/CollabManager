package com.richa.service;

import com.richa.exception.ProjectException;
import com.richa.exception.UserException;
import com.richa.model.User;

public interface UserService {

    public User findUserProfileByJwt(String jwt) throws UserException, ProjectException;

    public User findUserByEmail(String email) throws UserException;

    public User findUserById(Long userId) throws UserException;

    public User updateUsersProjectSize(User user,int number);


    void updatePassword(User user, String newPassword);

    void sendPasswordResetEmail(User user);


}
