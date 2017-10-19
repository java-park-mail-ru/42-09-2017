package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.messages.MessageConstants;

import javax.servlet.http.HttpSession;

import static ru.mail.park.info.constants.Constants.PASSWORD_MIN_LENGTH;

public class PasswordValidator extends Validator {
    @Override
    public String validate(Object obj, HttpSession httpSession, boolean nullable) {
        String password = (String) obj;

        if (!(!nullable && password == null) && password != null) {
            if (password.isEmpty()) {
                return MessageConstants.EMPTY_PASSWORD;
            }
            if (password.length() < PASSWORD_MIN_LENGTH) {
                return MessageConstants.BAD_PASSWORD;
            }
        }
        return null;
    }
}
