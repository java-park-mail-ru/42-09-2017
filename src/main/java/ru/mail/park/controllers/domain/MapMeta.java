package ru.mail.park.controllers.domain;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "map_meta")
public class MapMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer level;
    private Integer timer;
    private Timestamp created;
    private String preview;
    private Integer playedTimes;
    private Integer players;

    public MapMeta() {

    }

    public MapMeta(
            String name,
            Integer level,
            Integer timer,
            Timestamp created,
            String preview,
            Integer playedTimes,
            Integer players
    ) {
        this.name = name;
        this.level = level;
        this.timer = timer;
        this.created = created;
        this.preview = preview;
        this.playedTimes = playedTimes;
        this.players = players;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getTimer() {
        return timer;
    }

    public Timestamp getCreated() {
        return created;
    }

    public String getPreview() {
        return preview;
    }

    public Integer getPlayedTimes() {
        return playedTimes;
    }

    public Integer getPlayers() {
        return players;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
