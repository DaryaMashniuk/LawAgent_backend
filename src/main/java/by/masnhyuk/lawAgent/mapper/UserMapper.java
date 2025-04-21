package by.masnhyuk.lawAgent.mapper;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.Users;

public class UserMapper {

    public static UserDto mapToUserDto(Users users) {
        return new UserDto(
                users.getId(),
                users.getUsername(),
                users.getPassword(),
                users.getEmail(),
                users.getSubscription(),
                users.getCreatedAt(),
                users.getIsActive()
        );
    }

    public static Users mapToUser(UserDto userDto) {
        return new Users(
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
