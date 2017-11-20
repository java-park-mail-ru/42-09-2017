package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

public class FinishMessage extends SocketMessage {
    private Long score;

    @JsonCreator
    public FinishMessage() {
        this(0L);
    }

    public FinishMessage(Long score) {
        this.score = score;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}
