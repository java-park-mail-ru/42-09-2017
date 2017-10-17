package ru.mail.park.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.controllers.validators.NotExists;
import ru.mail.park.controllers.validators.Password;
import ru.mail.park.info.constants.Constants;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserUpdateInfo {
    @Size(
            min = Constants.USERNAME_MIN_LENGTH,
            message = MessageConstants.SHORT_USERNAME
    )
    @Pattern(
            regexp = Constants.USERNAME_REGEXP,
            message = MessageConstants.BAD_USERNAME
    )
    @NotExists(
            field = "username",
            message = MessageConstants.EXISTS_USERNAME
    )
    private final String username;

    @Pattern(
            regexp = Constants.EMAIL_REGEXP,
            message = MessageConstants.BAD_EMAIL
    )
    @NotExists(
            field = "email",
            message = MessageConstants.EXISTS_EMAIL
    )
    private final String email;

    private final String oldPassword;

    @Password
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
