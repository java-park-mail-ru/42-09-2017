package ru.mail.park.websocket.message.from;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.validation.annotation.Validated;
import ru.mail.park.controllers.validators.groups.SimulationSnap;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.websocket.message.SocketMessage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated(SimulationSnap.class)
public class SnapMessage extends SocketMessage {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Long frame;
    @Valid
    private List<BodyFrame> bodies;

    @JsonCreator
    public SnapMessage() {

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
