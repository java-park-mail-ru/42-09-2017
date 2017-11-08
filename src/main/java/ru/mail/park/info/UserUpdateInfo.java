package ru.mail.park.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Email;
import ru.mail.park.controllers.validators.constraints.CSize;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.controllers.validators.constraints.CNotBlank;
import ru.mail.park.info.constants.Constants;

import javax.validation.constraints.Pattern;

public class UserUpdateInfo {
    @CNotBlank(message = MessageConstants.EMPTY_USERNAME)
    @CSize(min = Constants.USERNAME_MIN_LENGTH, message = MessageConstants.SHORT_USERNAME)
    @Pattern(regexp = Constants.USERNAME_REGEXP, message = MessageConstants.BAD_USERNAME)
    private final String username;

    @CNotBlank(message = MessageConstants.EMPTY_EMAIL)
    @Email(message = MessageConstants.BAD_EMAIL)
    private final String email;
    private final String oldPassword;

    @CNotBlank(message = MessageConstants.EMPTY_PASSWORD)
    @CSize(min = Constants.PASSWORD_MIN_LENGTH, message = MessageConstants.BAD_PASSWORD)
    private final String password;

    @JsonCreator
    public UserUpdateInfo(
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("oldPassword") String oldPassword,
        @JsonProperty("password") String password
    ) {
        this.username = username;
        this.email = email;
        this.oldPassword = oldPassword;

        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getPassword() {
        return password;
    }
}
