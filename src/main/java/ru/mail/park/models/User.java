package ru.mail.park.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.controllers.validators.NotExists;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.view.View;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class User {
    @NotNull(message = MessageConstants.EMPTY_USERNAME)
    @Size(
            min = Constants.USERNAME_MIN_LENGTH,
            message = MessageConstants.SHORT_USERNAME
    )
    @Pattern(
            regexp = "[A-Za-z][A-Za-z0-9]*?([-_][A-Za-z0-9]+){0,2}",
            message = MessageConstants.BAD_USERNAME
    )
    @NotExists(
            field = "username",
            message = MessageConstants.EXISTS_USERNAME
    )
    @JsonView(View.Summary.class)
    private String username;

    @NotNull(message = MessageConstants.EMPTY_EMAIL)
    @Pattern(
            regexp = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$",
            message = MessageConstants.BAD_EMAIL
    )
    @NotExists(
            field = "email",
            message = MessageConstants.EXISTS_EMAIL
    )
    @JsonView(View.Summary.class)
    private String email;

    @NotNull(message = MessageConstants.EMPTY_PASSWORD)
    @Size(
            min = Constants.PASSWORD_MIN_LENGTH,
            message = MessageConstants.BAD_PASSWORD
    )
    private String password;

    @JsonCreator
    private User(
            @JsonProperty("username") String username,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(User user) {
        this(user.getUsername(), user.getEmail(), user.getPassword());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
