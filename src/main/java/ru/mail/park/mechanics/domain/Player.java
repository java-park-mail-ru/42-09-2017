package ru.mail.park.mechanics.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;

public class Player {
    private Id<Player> id;
    private User user;
    private boolean ready = false;
    private boolean finished = false;
    private Long score = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    public Player(
            User user
    ) {
        this.user = user;
    }

    public Id<Player> getId() {
        return id;
    }

    public void setId(Id<Player> id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setFinished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        if (this.score != 0L || score == 0L) {
            return;
        }
        LOGGER.warn("Setting score to player " + id);
        this.score = score;
        user.setScores(this.score);
    }
}
