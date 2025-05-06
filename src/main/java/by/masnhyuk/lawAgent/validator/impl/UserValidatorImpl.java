package by.masnhyuk.lawAgent.validator.impl;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserValidatorImpl implements by.masnhyuk.lawAgent.validator.UserValidator {

    private static final Logger logger = LoggerFactory.getLogger(UserValidatorImpl.class);
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{6,}$";
    private final UserRepository userRepository;

    public UserValidatorImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean validate(UserDto userDto) {
        if (userDto == null) {
            logger.error("UserDto is null");
            return false;
        }

        return isUsernameNotEmpty(userDto.getUsername()) &&
                isUsernameLengthValid(userDto.getUsername()) &&
                isUsernameAvailable(userDto.getUsername()) &&
                isPasswordNotEmpty(userDto.getPassword()) &&
                isPasswordLengthValid(userDto.getPassword()) &&
                isPasswordFormatValid(userDto.getPassword()) &&
                isEmailNotEmpty(userDto.getEmail()) &&
                isEmailFormatValid(userDto.getEmail()) &&
                isEmailAvailable(userDto.getEmail()) &&
                isSubscriptionNotEmpty(userDto.getSubscription());
    }

    @Override
    public boolean isUsernameNotEmpty(String username) {
        if (!StringUtils.hasText(username)) {
            logger.error("Username is required");
            return false;
        }
        return true;
    }

    @Override
    public boolean isUsernameLengthValid(String username) {
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            logger.error("Username must be {}-{} characters", MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH);
            return false;
        }
        return true;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            logger.error("Username {} already exists", username);
            return false;
        }
        return true;
    }

    @Override
    public boolean isPasswordNotEmpty(String password) {
        if (!StringUtils.hasText(password)) {
            logger.error("Password is required");
            return false;
        }
        return true;
    }

    @Override
    public boolean isPasswordLengthValid(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            logger.error("Password must be at least {} characters", MIN_PASSWORD_LENGTH);
            return false;
        }
        return true;
    }

    @Override
    public boolean isPasswordFormatValid(String password) {
        if (!password.matches(PASSWORD_PATTERN)) {
            logger.error("Password must contain at least 6 characters, including one letter, one number and one special character");
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmailNotEmpty(String email) {
        if (!StringUtils.hasText(email)) {
            logger.error("Email is required");
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmailFormatValid(String email) {
        if (!email.matches(EMAIL_PATTERN)) {
            logger.error("Invalid email format: {}", email);
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            logger.error("Email {} already registered", email);
            return false;
        }
        return true;
    }

    @Override
    public boolean isSubscriptionNotEmpty(String subscription) {
        if (!StringUtils.hasText(subscription)) {
            logger.error("Subscription is required");
            return false;
        }
        return true;
    }

    @Override
    public boolean validateLogin(String username, String password) {
        return isUsernameNotEmpty(username) &&
                isPasswordNotEmpty(password) &&
                userRepository.existsByUsername(username);
    }
}