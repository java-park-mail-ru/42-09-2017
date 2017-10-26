package ru.mail.park.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class MapMetaDto {
    private Integer id;
    private String name;
    private Integer level;
    private Integer timer;
    private Timestamp created;
    private String preview;
    private Integer playedTimes;
    private Integer players;

    public MapMetaDto(
            Integer id,
            String name,
            Integer level,
            Integer timer,
            Timestamp created,
            String preview,
            Integer playedTimes,
            Integer players
    ) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.timer = timer;
        this.created = created;
        this.preview = preview;
        this.playedTimes = playedTimes;
        this.players = players;
    }

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

    @JsonProperty("created")
    public Timestamp getCreated() {
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
}
