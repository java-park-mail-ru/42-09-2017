package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

import static ru.mail.park.info.constants.MessageConstants.GAME_ERROR;

public class FinishedMessage extends SocketMessage {
    private Long score;
    private String reason;

    @JsonCreator
    public FinishedMessage() {
        this(0L, GAME_ERROR);
    }

    public FinishedMessage(Long score, String reason) {
        this.score = score;
        this.reason = reason;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
