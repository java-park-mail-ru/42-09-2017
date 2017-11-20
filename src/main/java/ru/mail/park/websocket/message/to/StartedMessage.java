package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.websocket.message.SocketMessage;

public class StartedMessage extends SocketMessage {
    @JsonCreator
    public StartedMessage() {

    }
}
