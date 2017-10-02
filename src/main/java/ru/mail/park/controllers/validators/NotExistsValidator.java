package ru.mail.park.controllers.validators;

import ru.mail.park.services.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotExistsValidator implements ConstraintValidator<NotExists, String> {
    private final UserService userService;
    private String field;

    NotExistsValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(NotExists annotation) {
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(String login,
                           ConstraintValidatorContext cxt) {
        if (field.equals("username")) {
            return !userService.hasUsername(login);
        } else if (field.equals("email")) {
            return !userService.hasEmail(login);
        }
        return false;
    }
}
