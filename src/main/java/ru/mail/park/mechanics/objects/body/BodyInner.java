package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BodyInner {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Valid
    private BodyInnerData data;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private String type;

    @JsonCreator
    public BodyInner() {

    }

    public BodyInnerData getData() {
        return data;
    }

    public void setData(BodyInnerData data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}