package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.info.constants.Constants;

import javax.servlet.http.HttpSession;

public class OldPasswordValidator extends Validator {
    @Override
    public <T> String validate(Object obj, HttpSession httpSession, boolean nullable) {
        String oldPassword = (String) obj;
        if (!(!nullable && oldPassword == null) && oldPassword != null) {
            Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
            if (!getUserDao().checkUserPassword(getUserDao().findUserById(id), oldPassword)) {
                return MessageConstants.BAD_OLD_PASSWORD;
            }
        }
        return null;
    }
}
