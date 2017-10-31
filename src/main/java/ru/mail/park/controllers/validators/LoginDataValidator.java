package ru.mail.park.controllers.validators;

import org.springframework.beans.BeanWrapperImpl;
import ru.mail.park.domain.User;
import ru.mail.park.controllers.validators.constraints.LoginData;
import ru.mail.park.services.UserDao;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoginDataValidator implements ConstraintValidator<LoginData, Object> {
    private final UserDao userDao;

    public LoginDataValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void initialize(LoginData constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String login = (String) new BeanWrapperImpl(value).getPropertyValue("login");
        String password = (String) new BeanWrapperImpl(value).getPropertyValue("password");

        User user = userDao.findUserByUsername(login);
        if (user == null) {
            user = userDao.findUserByEmail(login);
        }

        return user == null || userDao.checkUserPassword(user, password);
    }
}
