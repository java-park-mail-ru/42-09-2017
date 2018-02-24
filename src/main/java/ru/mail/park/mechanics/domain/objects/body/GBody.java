package ru.mail.park.mechanics.domain.objects.body;

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

    @SuppressWarnings("unused")
    public Long getPlayerID() {
        return playerID;
    }

    @SuppressWarnings("unused")
    public void setPlayerID(Long playerID) {
        this.playerID = playerID;
    }

    public BodyData getData() {
        return data;
    }

    @SuppressWarnings("unused")
    public void setData(BodyData data) {
        this.data = data;
    }

    @SuppressWarnings("unused")
    public boolean isSelectable() {
        return selectable;
    }

    @SuppressWarnings("unused")
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public String getKind() {
        return kind;
    }

    @SuppressWarnings("unused")
    public void setKind(String kind) {
        this.kind = kind;
    }
}
