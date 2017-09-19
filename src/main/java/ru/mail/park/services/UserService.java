package ru.mail.park.services;

import org.springframework.stereotype.Service;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.info.UserSignupInfo;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private List<User> userList;

    public UserService() {
        userList = new ArrayList<>();
    }

    public List<User> getUserList() {
        return userList;
    }

    public User addUser(UserSignupInfo userSignupInfo) {
        User user = new User(
                userSignupInfo.getUsername(),
                userSignupInfo.getEmail(),
                userSignupInfo.getPassword()
        );
        userList.add(user);
        return user;
    }

    public User updateUser(User user, UserUpdateInfo userUpdateInfo) {
        User userInList = findUser(user.getUsername());
        final String username = userUpdateInfo.getUsername();
        final String email = userUpdateInfo.getEmail();
        final String password = userUpdateInfo.getPassword();

        if (username != null) {
            userInList.setUsername(username);
        }

        if (email != null) {
            userInList.setEmail(email);
        }

        if (password != null) {
            userInList.setPassword(password);
        }

        return userInList;
    }

    private User findUser(String username) {
        for (User user : userList) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public User checkUser(UserSigninInfo userSigninInfo) {
        User userFound = null;
        for (User user : userList) {
            String usernameOrEmail = userSigninInfo.getUsernameOrEmail();
            if ((usernameOrEmail.equals(user.getUsername())) || usernameOrEmail.equals(user.getEmail())) {
                userFound = user;
                break;
            }
        }
        if (userFound != null) {
            if (userSigninInfo.getPassword().equals(userFound.getPassword())) {
                return userFound;
            }
        }
        return null;
    }
}
