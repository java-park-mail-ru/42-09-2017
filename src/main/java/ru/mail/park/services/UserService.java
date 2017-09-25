package ru.mail.park.services;

import org.springframework.stereotype.Service;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private static Map<String, User> userMap;

    public UserService() {
        userMap = new HashMap<>();
    }

    public User addUser(User userSignupInfo) {
        User user = new User(userSignupInfo);
        userMap.put(userSignupInfo.getUsername(), user);
        return user;
    }

    public User updateUser(String username, UserUpdateInfo userUpdateInfo) {
        User user = getByUsername(username);
        final String usernameNew = userUpdateInfo.getUsername();
        final String emailNew = userUpdateInfo.getEmail();
        final String passwordNew = userUpdateInfo.getPassword();

        if (usernameNew != null) {
            user.setUsername(usernameNew);
        }

        if (emailNew != null) {
            user.setEmail(emailNew);
        }

        if (passwordNew != null) {
            user.setPassword(passwordNew);
        }

        return user;
    }

    public User getByUsername(String username) {
        return userMap.get(username);
    }

    public static boolean hasUsername(String username) {
        return userMap.containsKey(username);
    }

    public static boolean hasEmail(String email) {
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            if (entry.getValue().getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public User checkUserAndPassword(String usernameOrEmail, String password) {
        User userFound = null;
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            if (entry.getValue().getEmail().equals(usernameOrEmail) || entry.getValue().getUsername().equals(usernameOrEmail)) {
                userFound = entry.getValue();
                break;
            }
        }
        if (userFound != null) {
            if (password.equals(userFound.getPassword())) {
                return userFound;
            }
        }
        return null;
    }
}
