package ru.mail.park.controllers.validators;

import ru.mail.park.info.constants.Constants;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, Object> {
    @Override
    public void initialize(Password constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String password = (String) value;
        return password == null || password.isEmpty() || password.length() >= Constants.PASSWORD_MIN_LENGTH;
    }
}
