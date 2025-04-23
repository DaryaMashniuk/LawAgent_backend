package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.Users;
import by.masnhyuk.lawAgent.exception.ResourceNotFoundException;
import by.masnhyuk.lawAgent.mapper.UserMapper;
import by.masnhyuk.lawAgent.repository.UserRepository;
import by.masnhyuk.lawAgent.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Override
    public UserDto register(UserDto userDto) {
        Users user = UserMapper.mapToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Users registeredUser = userRepository.save(user);
        return UserMapper.mapToUserDto(registeredUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        Users users = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does'n exist with id: " + userId)
                );
        return UserMapper.mapToUserDto(users);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return users.stream().map((UserMapper::mapToUserDto)).toList();
    }

    @Override
    public String verify(UserDto user) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        return jwtService.generateToken(user.getUsername());
    }


}
