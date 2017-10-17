package ru.mail.park.services.dao;

import ru.mail.park.controllers.domain.User;
import ru.mail.park.info.UserUpdateInfo;

public interface UserDao {
    User createUser(User userData);

    User updateUser(User user, UserUpdateInfo userData);

    boolean checkUserPassword(User user, String password);

    User findUserById(Long id);

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    boolean hasUsername(String username);

    boolean hasEmail(String email);
}
