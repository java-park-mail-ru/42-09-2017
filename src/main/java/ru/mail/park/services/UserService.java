package ru.mail.park.services;

import org.springframework.stereotype.Service;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private List<User> userList;

    public UserService() {
        userList = new ArrayList<>();
    }

    public User addUser(User userSignupInfo) {
        User user = new User(
                userSignupInfo.getUsername(),
                userSignupInfo.getEmail(),
                userSignupInfo.getPassword()
        );
        userList.add(user);
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

    private User getByUsername(String username) {
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean hasUsername(String username) {
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEmail(String email) {
        for (User user : userList) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public User checkUserAndPassword(String usernameOrEmail, String password) {
        User userFound = null;
        for (User user : userList) {
            if ((usernameOrEmail.equals(user.getUsername())) || usernameOrEmail.equals(user.getEmail())) {
                userFound = user;
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
