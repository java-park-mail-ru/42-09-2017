package ru.mail.park.mechanics;

import org.hibernate.validator.constraints.NotEmpty;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.mechanics.domain.objects.BodyFrame;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.mail.park.info.constants.MessageConstants.GAME_ERROR;

public class GameSession {
    private Id<Board> boardId;
    @NotNull
    private BoardRequest.Data board;
    @NotEmpty
    private Set<Id<User>> players;
    private GameState state = GameState.NONE;
    private String result = GAME_ERROR;

    private final Map<Id<User>, List<BodyFrame>> initSnapsMap = new HashMap<>();

    public GameSession(
            @NotNull Id<Board> boardId,
            @NotNull BoardRequest.Data board,
            @NotEmpty Set<Id<User>> players
    ) {
        this.boardId = boardId;
        this.board = board;
        this.players = players;
    }

    public Id<Board> getBoardId() {
        return boardId;
    }

    public BoardRequest.Data getBoard() {
        return board;
    }

    public void setBoard(BoardRequest.Data board) {
        this.board = board;
    }

    public Set<Id<User>> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Id<User>> players) {
        this.players = players;
    }

    public void removePlayer(Id<User> userId) {
        players.remove(userId);
    }

    public boolean isMoving() {
        return state == GameState.MOVING;
    }

    public boolean isReady() {
        return state == GameState.READY;
    }

    public boolean isSimulated() {
        return state == GameState.SIMULATED;
    }

    public boolean isFinished() {
        return state == GameState.FINISHED;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Map<Id<User>, List<BodyFrame>> getInitSnapsMap() {
        return initSnapsMap;
    }

    public void putSnapFor(Id<User> userId, List<BodyFrame> snap) {
        initSnapsMap.putIfAbsent(userId, snap);
    }
}
