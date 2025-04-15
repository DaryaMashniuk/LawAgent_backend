package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.User;
import by.masnhyuk.lawAgent.exception.ResourceNotFoundException;
import by.masnhyuk.lawAgent.mapper.UserMapper;
import by.masnhyuk.lawAgent.repository.UserRepository;
import by.masnhyuk.lawAgent.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    public UserDto register(UserDto userDto) {
        User user = UserMapper.mapToUser(userDto);
        User registeredUser = userRepository.save(user);
        return UserMapper.mapToUserDto(registeredUser);
    }

    @Override
    public UserDto getUserBuId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does'n exist with id: " + userId)
                );
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map((UserMapper::mapToUserDto)).toList();
    }

}
