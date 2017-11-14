package ru.mail.park.mechanics.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.mechanics.objects.body.BodyFrame;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class ClientSnap {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Long frame;
    @Valid
    private List<BodyFrame> bodies;

    @JsonCreator
    public ClientSnap() {

    }

    public Long getFrame() {
        return frame;
    }

    public void setFrame(Long frame) {
        this.frame = frame;
    }

    public List<BodyFrame> getBodies() {
        return bodies;
    }

    public void setBodies(List<BodyFrame> bodies) {
        this.bodies = bodies;
    }
}
