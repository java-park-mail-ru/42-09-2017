package ru.mail.park.controllers.domain;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "map_meta")
public class MapMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer level;
    private Integer timer;
    private Integer rating;
    private Date created;
    private String preview;
    private Integer playedTimes;
    private Integer players;

    public MapMeta() {

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

    public Integer getRating() {
        return rating;
    }

    public Date getCreated() {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setTimer(Integer timer) {
        this.timer = timer;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setPlayedTimes(Integer playedTimes) {
        this.playedTimes = playedTimes;
    }

    public void setPlayers(Integer players) {
        this.players = players;
    }
}
