package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GBody {
    private Long id;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Valid
    private BodyInner body;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private String type;
    private boolean keyBody;

    @JsonCreator
    public GBody() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BodyInner getBody() {
        return body;
    }

    public void setBody(BodyInner body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isKeyBody() {
        return keyBody;
    }

    public void setKeyBody(boolean keyBody) {
        this.keyBody = keyBody;
    }
}
