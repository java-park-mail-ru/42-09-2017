package ru.mail.park.models;

import com.fasterxml.jackson.annotation.JsonView;
import ru.mail.park.view.View;

public class User {
    @JsonView(View.Summary.class)
    private String username;

    @JsonView(View.Summary.class)
    private String email;

    private String password;

    //@JsonCreator
    public User(
            /*@JsonProperty("username") */String username,
            /*@JsonProperty("email")*/ String email,
            /*@JsonProperty("password")*/ String password

    ) {
        this.username = username;
        this.email = email;
        this.password = password;
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
