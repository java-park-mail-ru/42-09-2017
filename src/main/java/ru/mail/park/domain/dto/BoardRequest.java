package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class BoardRequest {
    private BoardMetaDto boardMetaDto;
    private Map<String, Object> boardDataMap;

    @JsonCreator
    public BoardRequest() {

    }

    @JsonProperty("meta")
    public BoardMetaDto getBoardMetaDto() {
        return boardMetaDto;
    }

    public void setBoardMetaDto(BoardMetaDto boardMetaDto) {
        this.boardMetaDto = boardMetaDto;
    }

    @JsonProperty("data")
    public Map<String, Object> getBoardDataMap() {
        return boardDataMap;
    }

    public void setBoardDataMap(Map<String, Object> boardDataMap) {
        this.boardDataMap = boardDataMap;
    }
}
