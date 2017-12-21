package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

public class BoardMessage extends SocketMessage {
    private Long playerID;
    private String friend;
    private Integer level;

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

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
