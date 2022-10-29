package ru.practicum.ewm.model.user;

public class UserMapper {

    public static User toUser(NewUserRequest userRequest) {
        return new User(
                userRequest.getName(),
                userRequest.getEmail()
        );
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static UserShortDto toShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }
}