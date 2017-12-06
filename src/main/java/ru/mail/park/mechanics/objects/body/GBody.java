package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GBody {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Long id;
    private Long playerID;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Valid
    private BodyData data;
    private boolean selectable;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private String kind;

    @JsonCreator
    public GBody() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerID() {
        return playerID;
    }

    public void setPlayerID(Long playerID) {
        this.playerID = playerID;
    }

    public BodyData getData() {
        return data;
    }

    public void setData(BodyData data) {
        this.data = data;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
