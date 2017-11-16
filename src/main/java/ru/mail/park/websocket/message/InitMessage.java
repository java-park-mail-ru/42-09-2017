package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;

import javax.validation.constraints.NotNull;

public class InitMessage extends SocketMessage {
    @NotNull
    private Id<Board> board;

    @JsonCreator
    public InitMessage() {

    }

    public Id<Board> getBoard() {
        return board;
    }

    public void setBoard(Id<Board> board) {
        this.board = board;
    }
}
