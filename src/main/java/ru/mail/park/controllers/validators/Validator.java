package ru.mail.park.controllers.validators;

import org.springframework.stereotype.Service;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.services.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.mail.park.controllers.messages.MessageResources.*;

@Service
public class Validator {
    private final UserService userService;

    private static final Integer USERNAME_MIN_LENGTH = 3;
    private static final Integer PASSWORD_MIN_LENGTH = 6;

    public Validator(UserService userService) {
        this.userService = userService;
    }

    private static Pattern patternEmail = Pattern.compile("^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$");
    private static Pattern patternUsername = Pattern.compile("[A-Za-z][A-Za-z0-9]*?([-_][A-Za-z0-9]+){0,2}");

    public Message validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return EMPTY_EMAIL.getMessage();
        }

        final Matcher matcher = patternEmail.matcher(email);

        if (!matcher.matches()) {
            return BAD_EMAIL.getMessage();
        }

        if (userService.hasEmail(email)) {
            return EXISTS_EMAIL.getMessage();
        }

        return null;
    }

    public Message validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return EMPTY_USERNAME.getMessage();
        }

        if (username.length() < USERNAME_MIN_LENGTH) {
            return SHORT_USERNAME.getMessage();
        }

        final Matcher matcher = patternUsername.matcher(username);

        if (!matcher.matches()) {
            return BAD_USERNAME.getMessage();
        }

        if (userService.hasUsername(username)) {
            return EXISTS_USERNAME.getMessage();
        }

        return null;
    }

    public Message validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return EMPTY_PASSWORD.getMessage();
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            return BAD_PASSWORD.getMessage();
        }

        return null;
    }
}
