package ru.mail.park.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSigninInfo {
    private final String usernameOrEmail;
    private final String password;

    @JsonCreator
    public UserSigninInfo(
            @JsonProperty(value = "usernameOrEmail", required = true) String usernameOrEmail,
            @JsonProperty(value = "password", required = true) String password
    )
    {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }
}
