package by.masnhyuk.lawAgent.validator;

import by.masnhyuk.lawAgent.dto.UserDto;

public interface UserValidator {
    boolean validate(UserDto userDto);

    boolean isUsernameNotEmpty(String username);

    boolean isUsernameLengthValid(String username);

    boolean isUsernameAvailable(String username);

    boolean isPasswordNotEmpty(String password);

    boolean isPasswordLengthValid(String password);

    boolean isPasswordFormatValid(String password);

    boolean isEmailNotEmpty(String email);

    boolean isEmailFormatValid(String email);

    boolean isEmailAvailable(String email);

    boolean isSubscriptionNotEmpty(String subscription);

    boolean validateLogin(String username, String password);
}
