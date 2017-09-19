package ru.mail.park.response;

import com.fasterxml.jackson.annotation.JsonView;
import ru.mail.park.View.View;

public class ResponseBody {
    @JsonView(View.Summary.class)
    private final int status;

    @JsonView(View.Summary.class)
    private final String error;

    public ResponseBody(int status, String error) {
        this.status = status;
        this.error = error;
    }
}
