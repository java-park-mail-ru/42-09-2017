package ru.mail.park.dto.helpers;

import ru.mail.park.controllers.domain.User;
import ru.mail.park.dto.UserDTO;

public class UserHelper {
    public static UserDTO toDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        return userDTO;
    }

    public static User fromDto(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        return user;
    }
}
