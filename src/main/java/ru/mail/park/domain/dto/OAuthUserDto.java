package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OAuthUserDto {
    private Integer userId;
    private String username;

    @JsonCreator
    public OAuthUserDto() {

    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
