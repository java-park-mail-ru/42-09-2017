package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.info.constants.Constants;

import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator extends Validator {
    private static Pattern pattern = Pattern.compile(Constants.EMAIL_REGEXP);

    @Override
    public <T> String validate(Object obj, HttpSession httpSession, boolean nullable) {
        String email = (String) obj;

        if (!(!nullable && email == null) && email != null) {
            if (email.isEmpty()) {
                return MessageConstants.EMPTY_EMAIL;
            }

            final Matcher matcher = pattern.matcher(email);

            if (!matcher.matches()) {
                return MessageConstants.BAD_EMAIL;
            }
            if (getUserDao().hasEmail(email)) {
                return MessageConstants.EXISTS_EMAIL;
            }
        }
        return null;
    }
}
