package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StartMessage extends SocketMessage {
    private String start;

    @JsonCreator
    public StartMessage() {

    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }
}
