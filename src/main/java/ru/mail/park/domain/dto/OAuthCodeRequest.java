package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OAuthCodeRequest {
    private String code;

    @JsonCreator
    public OAuthCodeRequest() {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
