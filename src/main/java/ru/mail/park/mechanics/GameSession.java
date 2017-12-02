package ru.mail.park.mechanics;

import org.hibernate.validator.constraints.NotEmpty;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.objects.BodyFrame;

import javax.validation.constraints.NotNull;
import java.util.*;

import static ru.mail.park.info.constants.Constants.GRAVITY_X;
import static ru.mail.park.info.constants.Constants.GRAVITY_Y;

public class GameSession {
    @NotNull
    private Id<Board> boardId;
    @NotEmpty
    private Set<Id<User>> players;

    private World world = new World(new Vec2(GRAVITY_X, GRAVITY_Y));

    private GameState state = GameState.NONE;

    private Map<Id<User>, List<BodyFrame>> initSnapsMap = new HashMap<>();

    public GameSession(
            @NotNull Id<Board> boardId,
            @NotEmpty Set<Id<User>> players
    ) {
        this.boardId = boardId;
        this.players = players;
    }

    public Id<Board> getBoardId() {
        return boardId;
    }

    public void setBoardId(Id<Board> boardId) {
        this.boardId = boardId;
    }

    public Set<Id<User>> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Id<User>> players) {
        this.players = players;
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

    public Map<Id<User>, List<BodyFrame>> getInitSnapsMap() {
        return initSnapsMap;
    }

    public void putSnapFor(Id<User> userId, List<BodyFrame> snap) {
        initSnapsMap.putIfAbsent(userId, snap);
    }
}
