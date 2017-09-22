package ru.mail.park.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSigninInfo {
    private final String login;
    private final String password;

    @JsonCreator
    public UserSigninInfo(
            @JsonProperty(value = "login", required = true) String login,
            @JsonProperty(value = "password", required = true) String password
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
