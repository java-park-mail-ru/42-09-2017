package ru.mail.park.websocket.message.from;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.websocket.message.SocketMessage;

import javax.validation.constraints.NotNull;

public class SubscribeMessage extends SocketMessage {
    @NotNull
    private Id<Board> board;

    @JsonCreator
    public SubscribeMessage() {

    }

    public Id<Board> getBoard() {
        return board;
    }

    public void setBoard(Id<Board> board) {
        this.board = board;
    }
}
