package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.mail.park.mechanics.objects.body.BodyFrame;

public class MovingMessage extends SocketMessage {
    @JsonIgnoreProperties({"velocity"})
    private BodyFrame frame;

    @JsonCreator
    public MovingMessage() {

    }

    public MovingMessage(BodyFrame frame) {
        this.frame = frame;
    }

    public BodyFrame getFrame() {
        return frame;
    }

    public void setFrame(BodyFrame frame) {
        this.frame = frame;
    }
}
