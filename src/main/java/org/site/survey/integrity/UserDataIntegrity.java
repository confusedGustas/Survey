package org.site.survey.integrity;

import org.site.survey.dto.UserRequestDTO;
import org.site.survey.exception.RequestValidationException;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class UserDataIntegrity {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{8,}$");

    public void validateUserId(Integer id) {
        if (id == null || id <= 0) {
            throw new RequestValidationException("Invalid user ID");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new RequestValidationException("Username cannot be empty");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new RequestValidationException("Username must be 3-20 characters long and can only contain letters, numbers, underscores, and hyphens");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RequestValidationException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RequestValidationException("Invalid email format");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RequestValidationException("Password cannot be empty");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RequestValidationException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character");
        }
    }

    public void validateUserRequest(UserRequestDTO request) {
        if (request == null) {
            throw new RequestValidationException("User request cannot be null");
        }
        validateUsername(request.getUsername());
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
    }
} 