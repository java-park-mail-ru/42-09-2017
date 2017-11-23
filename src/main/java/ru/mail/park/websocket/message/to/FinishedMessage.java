package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

public class FinishedMessage extends SocketMessage {
    private Long score;

    @JsonCreator
    public FinishedMessage() {
        this(0L);
    }

    public FinishedMessage(Long score) {
        this.score = score;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}
