package by.masnhyuk.lawAgent.mapper;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.User;

public class UserMapper {

    public static UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getSubscription(),
                user.getCreatedAt(),
                user.getIsActive()
        );
    }

    public static User mapToUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getUsername(),
                userDto.getPassword(),
                userDto.getEmail(),
                userDto.getSubscription(),
                userDto.getCreatedAt(),
                userDto.getIsActive()
        );
    }
}
