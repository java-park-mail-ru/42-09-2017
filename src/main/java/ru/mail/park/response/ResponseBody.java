package ru.mail.park.response;

import com.fasterxml.jackson.annotation.JsonView;
import ru.mail.park.view.View;

public class ResponseBody {
    @JsonView(View.Summary.class)
    private final int code;

    @JsonView(View.Summary.class)
    private final String message;

    public ResponseBody(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
