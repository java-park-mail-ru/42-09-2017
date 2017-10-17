package ru.mail.park.controllers.validators;

import ru.mail.park.services.dao.UserDao;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotExistsValidator implements ConstraintValidator<NotExists, String> {
    private final UserDao userDao;
    private String field;

    NotExistsValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void initialize(NotExists annotation) {
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(String login,
                           ConstraintValidatorContext cxt) {
        if (field.equals("username")) {
            return !userDao.hasUsername(login);
        } else if (field.equals("email")) {
            return !userDao.hasEmail(login);
        }
        return false;
    }
}
