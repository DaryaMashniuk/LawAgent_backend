package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.UserSubscription;
import by.masnhyuk.lawAgent.entity.Users;
import by.masnhyuk.lawAgent.exception.AuthenticationFailedException;
import by.masnhyuk.lawAgent.mapper.UserMapper;
import by.masnhyuk.lawAgent.repository.UserRepository;
import by.masnhyuk.lawAgent.repository.UserSubscriptionRepository;
import by.masnhyuk.lawAgent.service.UserService;
import by.masnhyuk.lawAgent.validator.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserValidator userValidator;
    private final JWTServiceImpl jwtServiceImpl;
    private final UserSubscriptionRepository subscriptionRepository;


    @Override
    public Optional<UserDto> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::mapToUserDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }


    @Override
    public Optional<UserDto> register(UserDto userDto) {
        if (!userValidator.validate(userDto)) {
            return Optional.empty();
        }

        Users user = UserMapper.mapToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Users registeredUser = userRepository.save(user);

        // Создаем подписку по умолчанию (например, на все категории, или только на одну)
        for (DocumentCategory category : DocumentCategory.values()) {
            UserSubscription subscription = new UserSubscription();
            subscription.setUser(registeredUser);
            subscription.setCategory(category);
            subscription.setIsActive(true);
            subscriptionRepository.save(subscription);
        }

        return Optional.of(UserMapper.mapToUserDto(registeredUser));
    }


    @Override
    public Optional<String> login(String username, String password) {
        try {
            if (!userValidator.validateLogin(username, password)) {
                return Optional.empty();
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            if (authentication.isAuthenticated()) {
                return Optional.of(jwtServiceImpl.generateToken(username));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new AuthenticationFailedException("Invalid username or password");
        }
    }
    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        Users existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Обновляем только разрешенные поля
        if (userDto.getUsername() != null && !userDto.getUsername().isEmpty()) {
            if (!userRepository.existsByUsernameAndIdNot(userDto.getUsername(), userId)) {
                existingUser.setUsername(userDto.getUsername());
            } else {
                throw new IllegalArgumentException("Username already exists");
            }
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            if (!userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                existingUser.setEmail(userDto.getEmail());
            } else {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        if (userDto.getSubscription() != null) {
            existingUser.setSubscription(userDto.getSubscription());
        }

        Users updatedUser = userRepository.save(existingUser);
        return UserMapper.mapToUserDto(updatedUser);
    }
}
