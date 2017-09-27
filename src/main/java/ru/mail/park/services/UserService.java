package ru.mail.park.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private Map<String, User> userMap;
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        userMap = new HashMap<>();
        this.passwordEncoder = passwordEncoder;
    }

    public User addUser(User userSignupInfo) {
        String passwordEncoded = passwordEncoder.encode(userSignupInfo.getPassword());
        userSignupInfo.setPassword(passwordEncoded);
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
            user.setPassword(passwordEncoder.encode(passwordNew));
        }

        return user;
    }

    public User getByUsername(String username) {
        return userMap.get(username);
    }

    public boolean hasUsername(String username) {
        return userMap.containsKey(username);
    }

    public boolean hasEmail(String email) {
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
            if (passwordEncoder.matches(password, userFound.getPassword())) {
                return userFound;
            }
        }
        return null;
    }
}
