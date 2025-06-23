package com.richa.service;

import com.richa.model.PasswordResetToken;

public interface PasswordResetTokenService {

    public PasswordResetToken findByToken(String token);

    public void delete(PasswordResetToken resetToken);

}
