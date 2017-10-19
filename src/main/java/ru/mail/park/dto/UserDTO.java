package ru.mail.park.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.mail.park.controllers.validators.CValidator;

public class UserDTO {
    private String username;
    private String email;
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

    @CValidator(fieldName = "username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @CValidator(fieldName = "email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @CValidator(fieldName = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }
}
