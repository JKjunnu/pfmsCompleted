package com.nal.pfms.backend.services;

import com.nal.pfms.backend.dtos.CredentialsDto;
import com.nal.pfms.backend.dtos.RegisterDto;
import com.nal.pfms.backend.dtos.UserDto;
import  com.nal.pfms.backend.entity.User;
import com.nal.pfms.backend.entity.VerificationToken;

import java.util.Optional;

public interface UserService {
    User registerUser(RegisterDto registerDto);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);

    boolean validateUserPassword(User user, CredentialsDto credentialsDto);

    UserDto findByLogin(String email);
}
