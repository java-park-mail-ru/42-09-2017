package ru.mail.park.websocket.message.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.websocket.message.SocketMessage;

public class BoardMessage extends SocketMessage {
    private Long id;
    private BoardRequest.Data board;

    @JsonCreator
    public BoardMessage() {

    }

    public BoardMessage(BoardRequest.Data board) {
        this(1L, board);
    }

    public BoardMessage(Long id, BoardRequest.Data board) {
        this.id = id;
        this.board = board;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BoardRequest.Data getBoard() {
        return board;
    }

    public void setBoard(BoardRequest.Data board) {
        this.board = board;
    }
}
