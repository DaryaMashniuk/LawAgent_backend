package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<UserDto> getUserById(Long userId);
    List<UserDto> getAllUsers();
    Optional<UserDto> register(UserDto userDto);
    Optional<String> login(String username, String password);
    UserDto updateUser(Long userId, UserDto userDto);
}