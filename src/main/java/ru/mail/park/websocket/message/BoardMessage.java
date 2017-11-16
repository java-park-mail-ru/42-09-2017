package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.domain.dto.BoardRequest;

public class BoardMessage extends SocketMessage {
    private BoardRequest.Data board;

    @JsonCreator
    public BoardMessage() {

    }

    public BoardMessage(BoardRequest.Data board) {
        this.board = board;
    }

    public BoardRequest.Data getBoard() {
        return board;
    }

    public void setBoard(BoardRequest.Data board) {
        this.board = board;
    }
}
