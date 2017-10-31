package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MapRequest {
    private MapMetaDto mapMeta;
    private Map<String, Object> mapData;

    @JsonCreator
    public MapRequest() {

    }

    @JsonProperty("meta")
    public MapMetaDto getMapMeta() {
        return mapMeta;
    }

    public void setMapMeta(MapMetaDto mapMeta) {
        this.mapMeta = mapMeta;
    }

    @JsonProperty("data")
    public Map<String, Object> getMapData() {
        return mapData;
    }

    public void setMapData(Map<String, Object> mapData) {
        this.mapData = mapData;
    }
}
