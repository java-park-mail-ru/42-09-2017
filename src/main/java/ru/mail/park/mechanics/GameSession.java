package ru.mail.park.mechanics;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.mechanics.objects.ClientSnap;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.mail.park.info.constants.Constants.GRAVITY_X;
import static ru.mail.park.info.constants.Constants.GRAVITY_Y;

public class GameSession {
    @NotNull
    private Id<User> first;
    private Id<User> second;
    @NotNull
    private Id<Board> boardId;
    private World world = new World(new Vec2(GRAVITY_X, GRAVITY_Y));
    private boolean simulating = false;
    private Map<Id<User>, ClientSnap> initSnapsMap = new HashMap<>();

    public GameSession(
            @NotNull Id<User> first,
            Id<User> second,
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

    public boolean isSimulating() {
        return simulating;
    }

    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }

    public Map<Id<User>, ClientSnap> getInitSnapsMap() {
        return initSnapsMap;
    }

    public void putSnapFor(Id<User> userId, ClientSnap snap) {
        initSnapsMap.putIfAbsent(userId, snap);
    }

    public List<Id<User>> getPlayers() {
        return Arrays.asList(first, second);
    }
}
