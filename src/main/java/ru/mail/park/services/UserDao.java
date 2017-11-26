package ru.mail.park.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.domain.User;
import ru.mail.park.exceptions.ControllerValidationException;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.info.constants.MessageConstants;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserDao {
    private final PasswordEncoder passwordEncoder;
    private EntityManager em;

    private static final long MILLION = 1000000;

    public UserDao(
            PasswordEncoder passwordEncoder,
            EntityManager em
    ) {
        this.passwordEncoder = passwordEncoder;
        this.em = em;
    }

    public User createUserVk(Integer userId, String token) {
        String pattern = "Player";
        long count = 0;
        String username;
        while (true) {
            username = pattern + String.valueOf(count);
            if (!hasUsername(username)) {
                User user = new User();
                user.setUsername(username);
                user.setVkId(userId);
                em.persist(user);
                return user;
            }
            count++;
            if (count > MILLION) {
                return null;
                // ToDo: 26.11.17 throw an exception
            }
        }
    }

    public void createUser(User userData) {
        List<String> errors = checkIfNotExists(userData.getUsername(), userData.getEmail());
        if (!errors.isEmpty()) {
            throw new ControllerValidationException(errors);
        }
        final String passwordEncoded = passwordEncoder.encode(userData.getPassword());
        userData.setPassword(passwordEncoded);
        em.persist(userData);
    }

    public void updateUserVk(User user, String token) {
        if (token != null) {
            user.setVkToken(token);
        }
    }

    public User updateUser(User user, UserUpdateInfo userData) {
        final String username = userData.getUsername();
        final String email = userData.getEmail();
        final String password = userData.getPassword();
        List<String> errors = checkIfNotExists(username, email);
        if (!errors.isEmpty()) {
            throw new ControllerValidationException(errors);
        }
        if (username != null) {
            user.setUsername(username);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        return user;
    }

    public User prepareSignIn(UserSigninInfo userSigninInfo) {
        List<String> errors = checkIfExists(userSigninInfo.getLogin());
        if (!errors.isEmpty()) {
            throw new ControllerValidationException(errors);
        }
        User user = findUserByUsername(userSigninInfo.getLogin());
        if (user == null) {
            user = findUserByEmail(userSigninInfo.getLogin());
        }
        if (!checkUserPassword(user, userSigninInfo.getPassword())) {
            errors.add(MessageConstants.PASSWORD_WRONG);
            throw new ControllerValidationException(errors);
        }
        return user;
    }

    public List<String> checkIfExists(String login) {
        List<String> errors = new ArrayList<>();
        if (!hasUsername(login) && !hasEmail(login)) {
            errors.add(MessageConstants.USERNAME_NOT_EXISTS);
        }
        return errors;
    }

    public List<String> checkIfNotExists(String username, String email) {
        List<String> errors = new ArrayList<>();
        if (username != null && hasUsername(username)) {
            errors.add(MessageConstants.EXISTS_USERNAME);
        }
        if (email != null && hasEmail(email)) {
            errors.add(MessageConstants.EXISTS_EMAIL);
        }
        return errors;
    }

    public boolean checkUserPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User findUserById(Long id) {
        return em.find(User.class, id);
    }

    public User findUserByUsername(String username) {
        try {
            return em.createQuery("select u from User as u where lower(username)=lower(:username)", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findUserByEmail(String email) {
        try {
            return em.createQuery("select u from User as u where lower(email)=lower(:email)", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean hasUsername(String username) {
        Long count = em.createQuery("select count(id) from User where lower(username)=lower(:username)", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    public boolean hasEmail(String email) {
        Long count = em.createQuery("select count(id) from User where lower(email)=lower(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    public User findUserVkById(Integer id) {
        try {
            return em.createQuery("select u from User as u where vk_id = :id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findUserVkByToken(String token) {
        try {
            return em.createQuery("select u from User as u where vk_token = :token", User.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
