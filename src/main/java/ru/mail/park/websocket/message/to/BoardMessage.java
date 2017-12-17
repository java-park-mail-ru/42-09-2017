package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

public class BoardMessage extends SocketMessage {
    private Long playerID;

    @JsonCreator
    public BoardMessage() {
        this(1L);
    }

    public BoardMessage(Long playerID) {
        this.playerID = playerID;
    }

    @SuppressWarnings("unused")
    public Long getPlayerID() {
        return playerID;
    }

    public void setPlayerID(Long playerID) {
        this.playerID = playerID;
    }
}
