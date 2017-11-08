package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GBody {
    private Long id;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Valid
    private BodyInner body;
    private boolean selectable;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Min(value = 0, message = MessageConstants.TYPE_OUT_OF_BOUNDS)
    @Max(value = 2, message = MessageConstants.TYPE_OUT_OF_BOUNDS)
    private Integer type;
    private boolean keyBody;
    private Integer keyBodyId;

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

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isKeyBody() {
        return keyBody;
    }

    public void setKeyBody(boolean keyBody) {
        this.keyBody = keyBody;
    }

    public Integer getKeyBodyId() {
        return keyBodyId;
    }

    public void setKeyBodyId(Integer keyBodyId) {
        this.keyBodyId = keyBodyId;
    }
}
