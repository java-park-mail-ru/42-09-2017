package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import ru.mail.park.controllers.validators.constraints.CSize;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.info.constants.Constants;

import javax.validation.constraints.Pattern;

public class UserDto {
    @NotBlank(message = MessageConstants.EMPTY_USERNAME)
    @CSize(min = Constants.USERNAME_MIN_LENGTH, message = MessageConstants.SHORT_USERNAME)
    @Pattern(regexp = Constants.USERNAME_REGEXP, message = MessageConstants.BAD_USERNAME)
    private String username;

    @NotBlank(message = MessageConstants.EMPTY_EMAIL)
    @Email(message = MessageConstants.BAD_EMAIL)
    private String email;

    @NotBlank(message = MessageConstants.EMPTY_PASSWORD)
    @CSize(min = Constants.PASSWORD_MIN_LENGTH, message = MessageConstants.BAD_PASSWORD)
    private String password;

    @JsonCreator
    public UserDto() {

    }

    public UserDto(
            String username,
            String email,
            String password
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
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
