package ru.mail.park.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.validator.constraints.NotBlank;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.controllers.validators.NotExists;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.view.View;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserDTO {
    @NotNull(message = MessageConstants.EMPTY_USERNAME)
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
    @JsonView(View.Summary.class)
    private String username;

    @NotBlank(message = MessageConstants.EMPTY_EMAIL)
    @Pattern(
            regexp = Constants.EMAIL_REGEXP,
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
    public UserDTO() {

    }

    public UserDTO(
            String username,
            String email,
            String password
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UserDTO(UserDTO user) {
        this(user.getUsername(), user.getEmail(), user.getPassword());
    }

    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }
}
