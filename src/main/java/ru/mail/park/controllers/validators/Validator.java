package ru.mail.park.controllers.validators;

import ru.mail.park.ResponseCodes;
import ru.mail.park.response.ResponseBody;
import ru.mail.park.services.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private final UserService userService;

    private static final Integer USERNAME_MIN_LENGTH = 3;

    private static final String USERNAME_REGEX = "^[a-z][a-z0-9]*?([-_][a-z0-9]+){0,2}$";
    private static final String EMAIL_REGEX =
            "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\""
            + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")"
            + "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|"
            + "\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
            + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
            + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[a-zA-Z])(?=.*[@#$%]).{8,20})";

    public Validator(UserService userService) {
        this.userService = userService;
    }

    public ResponseBody validateEmail(String email) {
        final Pattern pattern = Pattern.compile(EMAIL_REGEX);
        final Matcher matcher = pattern.matcher(email);

        if (email.isEmpty()) {
            return new ResponseBody(ResponseCodes.EMAIL_FIELD_EMPTY, "EMAIL_FIELD_EMPTY");
        }

        if (!matcher.matches()) {
            return new ResponseBody(ResponseCodes.EMAIL_FIELD_BAD, "EMAIL_FIELD_BAD");
        }

        if (userService.hasEmail(email)) {
            return new ResponseBody(ResponseCodes.EMAIL_ALREADY_EXISTS, "EMAIL_ALREADY_EXISTS");
        }

        return null;
    }

    public ResponseBody validateUsername(String username) {
        final Pattern pattern = Pattern.compile(USERNAME_REGEX);
        final Matcher matcher = pattern.matcher(username);

        if (username.isEmpty()) {
            return new ResponseBody(ResponseCodes.USERNAME_FIELD_EMPTY, "USERNAME_FIELD_EMPTY");
        }

        if (username.length() < USERNAME_MIN_LENGTH) {
            return new ResponseBody(ResponseCodes.USERNAME_FIELD_TOO_SHORT, "USERNAME_FIELD_TOO_SHORT");
        }

        if (!matcher.matches()) {
            return new ResponseBody(ResponseCodes.USERNAME_FIELD_BAD, "USERNAME_FIELD_BAD");
        }

        if (userService.hasUsername(username)) {
            return new ResponseBody(ResponseCodes.USERNAME_ALREADY_EXISTS, "USERNAME_ALREADY_EXISTS");
        }

        return null;
    }

    public ResponseBody validatePassword(String password) {
        final Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        final Matcher matcher = pattern.matcher(password);

        if (password.isEmpty()) {
            return new ResponseBody(ResponseCodes.PASSWORD_FIELD_EMPTY, "PASSWORD_FIELD_EMPTY");
        }

        if (!matcher.matches()) {
            return new ResponseBody(ResponseCodes.PASSWORD_FIELD_BAD, "PASSWORD_FIELD_BAD");
        }

        return null;
    }
}
