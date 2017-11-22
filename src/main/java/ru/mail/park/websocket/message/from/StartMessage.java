package ru.mail.park.websocket.message.from;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.validation.annotation.Validated;
import ru.mail.park.controllers.validators.groups.SimulationSnap;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.websocket.message.SocketMessage;

import javax.validation.Valid;
import java.util.List;

public class StartMessage extends SocketMessage {
    @Valid
    private List<BodyFrame> bodies;

    @JsonCreator
    public StartMessage() {

    }

    public StartMessage(
            List<BodyFrame> bodies
    ) {
        this.bodies = bodies;
    }

    public List<BodyFrame> getBodies() {
        return bodies;
    }

    public void setBodies(List<BodyFrame> bodies) {
        this.bodies = bodies;
    }
}
