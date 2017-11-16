package ru.mail.park.mechanics;

import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;

import javax.validation.constraints.NotNull;

public class GameSession {
    @NotNull
    private Id<User> first;
    @NotNull
    private Id<User> second;
    @NotNull
    private Id<Board> boardId;

    public GameSession(
            @NotNull Id<User> first,
            @NotNull Id<User> second,
            @NotNull Id<Board> boardId
    ) {
        this.first = first;
        this.second = second;
        this.boardId = boardId;
    }

    public Id<User> getFirst() {
        return first;
    }

    public void setFirst(Id<User> first) {
        this.first = first;
    }

    public Id<User> getSecond() {
        return second;
    }

    public void setSecond(Id<User> second) {
        this.second = second;
    }

    public Id<Board> getBoardId() {
        return boardId;
    }

    public void setBoardId(Id<Board> boardId) {
        this.boardId = boardId;
    }
}
