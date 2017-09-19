package ru.mail.park.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;

public class UserSignupInfo {
    private final String username;
    private final String email;
    private final String password;
    private final String confirmation;

    @JsonCreator
    UserSignupInfo(
            @JsonProperty(value = "username", required = true) String username,
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "password", required = true) String password,
            @JsonProperty(value = "confirmation", required = true) String confirmation
    )
    {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmation = confirmation;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmation() {
        return confirmation;
    }
}
