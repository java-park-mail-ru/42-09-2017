package ru.mail.park.domain.dto.helpers;

import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.UserDto;

public class UserHelper {
    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        String email = user.getEmail();
        if (email != null) {
            userDto.setEmail(email);
        }
        return userDto;
    }

    public static User fromDto(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        String email = userDto.getEmail();
        if (email != null) {
            user.setEmail(email);
        }
        user.setPassword(userDto.getPassword());
        return user;
    }
}
