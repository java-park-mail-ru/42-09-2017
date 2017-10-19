package ru.mail.park.controllers.validators;

import com.sun.istack.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mail.park.services.UserDao;

import javax.servlet.http.HttpSession;

public abstract class Validator {
    @Autowired
    private UserDao userDao;

    public <T> String validate(Object obj, @Nullable HttpSession httpSession, @Nullable boolean nullable) {
        return null;
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
