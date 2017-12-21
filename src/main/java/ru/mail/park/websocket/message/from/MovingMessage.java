package ru.mail.park.websocket.message.from;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.websocket.message.SocketMessage;

public class MovingMessage extends SocketMessage {
    @JsonIgnoreProperties({"linVelocity", "angVelocity"})
    private BodyFrame snap;

    @JsonCreator
    public MovingMessage() {

    }

    public MovingMessage(BodyFrame snap) {
        this.snap = snap;
    }

    public BodyFrame getSnap() {
        return snap;
    }

    public void setSnap(BodyFrame snap) {
        this.snap = snap;
    }
}
