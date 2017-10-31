package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.validators.constraints.Exists;
import ru.mail.park.services.UserDao;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExistsValidator implements ConstraintValidator<Exists, String> {
    private final UserDao userDao;

    ExistsValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void initialize(Exists annotation) {

    }

    @Override
    public boolean isValid(String login, ConstraintValidatorContext cxt) {
        if (login == null || login.isEmpty()) {
            return true;
        }

        return userDao.hasUsername(login) || userDao.hasEmail(login);
    }
}
