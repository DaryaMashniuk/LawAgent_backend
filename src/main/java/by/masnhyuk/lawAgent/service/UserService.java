package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto register(UserDto userDto);
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();

}
