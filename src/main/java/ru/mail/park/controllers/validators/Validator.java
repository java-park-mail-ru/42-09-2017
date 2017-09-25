package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.messages.Message;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.services.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private static final Integer USERNAME_MIN_LENGTH = 3;
    private static final Integer PASSWORD_MIN_LENGTH = 6;

    private static Pattern patternEmail = Pattern.compile(
            "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$"
    );
    private static Pattern patternUsername = Pattern.compile("[A-Za-z][A-Za-z0-9]*?([-_][A-Za-z0-9]+){0,2}");

    public static Message validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return new Message(MessageConstants.EMPTY_EMAIL);
        }

        final Matcher matcher = patternEmail.matcher(email);

        if (!matcher.matches()) {
            return new Message(MessageConstants.BAD_EMAIL);
        }

        if (UserService.hasEmail(email)) {
            return new Message(MessageConstants.EXISTS_EMAIL);
        }

        return null;
    }

    public static Message validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return new Message(MessageConstants.EMPTY_USERNAME);
        }

        if (username.length() < USERNAME_MIN_LENGTH) {
            return new Message(MessageConstants.SHORT_USERNAME);
        }

        final Matcher matcher = patternUsername.matcher(username);

        if (!matcher.matches()) {
            return new Message(MessageConstants.BAD_USERNAME);
        }

        if (UserService.hasUsername(username)) {
            return new Message(MessageConstants.EXISTS_USERNAME);
        }

        return null;
    }

    public static Message validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new Message(MessageConstants.EMPTY_PASSWORD);
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            return new Message(MessageConstants.BAD_PASSWORD);
        }

        return null;
    }
}
