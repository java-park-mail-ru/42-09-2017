package ru.mail.park.exceptions;

import java.util.List;

public class ControllerValidationException extends RuntimeException {
    private final List<String> responseList;

    public ControllerValidationException(List<String> responseList) {
        this.responseList = responseList;
    }

    public List<String> getResponseList() {
        return responseList;
    }
}
