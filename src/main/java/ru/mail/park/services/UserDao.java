package ru.mail.park.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.controllers.domain.User;
import ru.mail.park.info.UserUpdateInfo;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

@Service
@Transactional
public class UserDao {
    private final PasswordEncoder passwordEncoder;
    private EntityManager em;

    public UserDao(
            PasswordEncoder passwordEncoder,
            EntityManager em
    ) {
        this.passwordEncoder = passwordEncoder;
        this.em = em;
    }

    public User createUser(User userData) {
        final String passwordEncoded = passwordEncoder.encode(userData.getPassword());
        userData.setPassword(passwordEncoded);
        em.persist(userData);
        return userData;
    }

    public User updateUser(User user, UserUpdateInfo userData) {
        final String username = userData.getUsername();
        final String email = userData.getEmail();
        final String password = userData.getPassword();

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

    public boolean checkUserPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User findUserById(Long id) {
        return em.find(User.class, id);
    }

    public User findUserByUsername(String username) {
        try {
            return em.createQuery("select u from User as u where username=:username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findUserByEmail(String email) {
        try {
            return em.createQuery("select u from User as u where email=:email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean hasUsername(String username) {
        Long count = em.createQuery("select count(id) from User where username=:username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    public boolean hasEmail(String email) {
        Long count = em.createQuery("select count(id) from User where email=:email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }
}
