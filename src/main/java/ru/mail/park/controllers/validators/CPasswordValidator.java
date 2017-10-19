package ru.mail.park.controllers.validators;

import ru.mail.park.info.constants.Constants;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CPasswordValidator implements ConstraintValidator<CPassword, Object> {
    @Override
    public void initialize(CPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String password = (String) value;
        return password == null || password.isEmpty() || password.length() >= Constants.PASSWORD_MIN_LENGTH;
    }
}
