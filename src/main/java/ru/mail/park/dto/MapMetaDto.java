package ru.mail.park.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;

public class MapMetaDto {
    private Integer id;
    private String name;
    private Integer level;
    private Integer timer;
    private Integer rating;
    private Date created;
    private String preview;
    private Integer playedTimes;
    private Integer players;

    @JsonCreator
    public MapMetaDto() {

    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("level")
    public Integer getLevel() {
        return level;
    }

    @JsonProperty("timer")
    public Integer getTimer() {
        return timer;
    }

    @JsonProperty("rating")
    public Integer getRating() {
        return rating;
    }

    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    @JsonProperty("preview")
    public String getPreview() {
        return preview;
    }

    @JsonProperty("playedTimes")
    public Integer getPlayedTimes() {
        return playedTimes;
    }

    @JsonProperty("players")
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
