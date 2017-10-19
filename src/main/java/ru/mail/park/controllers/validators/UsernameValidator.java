package ru.mail.park.controllers.validators;

import com.sun.istack.internal.Nullable;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.info.constants.Constants;

import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsernameValidator extends Validator {
    private static Pattern pattern = Pattern.compile(Constants.USERNAME_REGEXP);

    @Override
    public <T> String validate(Object obj, HttpSession httpSession, boolean nullable) {
        String username = (String) obj;

        if (!(!nullable && username == null) && username != null) {
            if (username.isEmpty()) {
                return MessageConstants.EMPTY_USERNAME;
            }
            if (username.length() < Constants.USERNAME_MIN_LENGTH) {
                return MessageConstants.SHORT_USERNAME;
            }

            final Matcher matcher = pattern.matcher(username);

            if (!matcher.matches()) {
                return MessageConstants.BAD_USERNAME;
            }
            if (getUserDao().hasUsername(username)) {
                return MessageConstants.EXISTS_USERNAME;
            }
        }
        return null;
    }
}
