package ru.mail.park.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.controllers.validators.constraints.Exists;
import ru.mail.park.controllers.validators.constraints.LoginData;

@LoginData(message = MessageConstants.PASSWORD_WRONG)
public class UserSigninInfo {
    @NotBlank(message = MessageConstants.EMPTY_USERNAME)
    @Exists(message = MessageConstants.USERNAME_NOT_EXISTS)
    private final String login;
    @NotBlank(message = MessageConstants.EMPTY_PASSWORD)
    private final String password;

    @JsonCreator
    public UserSigninInfo(
            @JsonProperty("login") String login,
            @JsonProperty("password") String password
    ) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
