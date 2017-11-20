package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.websocket.message.SocketMessage;

public class BoardMessage extends SocketMessage {
    private Long id;

    @JsonCreator
    public BoardMessage() {
        this(1L);
    }

    public BoardMessage(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
