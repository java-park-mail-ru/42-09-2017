package ru.mail.park.controllers.validators;

import ru.mail.park.services.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private final UserService userService;

    private static final Integer USERNAME_MIN_LENGTH = 3;

    private static final String USERNAME_REGEX = "^[a-z][a-z0-9]*?([-_][a-z0-9]+){0,2}$";
    private static final String EMAIL_REGEX = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
    private static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[a-zA-Z])(?=.*[@#$%]).{8,20})";

    public Validator(UserService userService) {
        this.userService = userService;
    }

    private static Pattern patternEmail = Pattern.compile(EMAIL_REGEX);
    private static Pattern patternUsername = Pattern.compile(USERNAME_REGEX);
    private static Pattern patternPassword = Pattern.compile(PASSWORD_REGEX);

    public String validateEmail(String email) {
        final Matcher matcher = patternEmail.matcher(email);

        if (email.isEmpty()) {
            return "EMAIL_FIELD_EMPTY";
        }

        if (!matcher.matches()) {
            return "EMAIL_FIELD_BAD";
        }

        if (userService.hasEmail(email)) {
            return "EMAIL_ALREADY_EXISTS";
        }

        return null;
    }

    public String validateUsername(String username) {
        final Matcher matcher = patternUsername.matcher(username);

        if (username.isEmpty()) {
            return "USERNAME_FIELD_EMPTY";
        }

        if (username.length() < USERNAME_MIN_LENGTH) {
            return "USERNAME_FIELD_TOO_SHORT";
        }

        if (!matcher.matches()) {
            return "USERNAME_FIELD_BAD";
        }

        if (userService.hasUsername(username)) {
            return "USERNAME_ALREADY_EXISTS";
        }

        return null;
    }

    public String validatePassword(String password) {
        final Matcher matcher = patternPassword.matcher(password);

        if (password.isEmpty()) {
            return "PASSWORD_FIELD_EMPTY";
        }

        if (!matcher.matches()) {
            return "PASSWORD_FIELD_BAD";
        }

        return null;
    }
}
