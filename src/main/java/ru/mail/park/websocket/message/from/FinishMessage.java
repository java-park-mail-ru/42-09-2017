package ru.mail.park.websocket.message.from;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

public class FinishMessage extends SocketMessage {
    private Long frames;

    @JsonCreator
    public FinishMessage() {

    }

    public Long getFrames() {
        return frames;
    }

    public void setFrames(Long frames) {
        this.frames = frames;
    }
}
