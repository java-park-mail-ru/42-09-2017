package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public class BoardResponse {
    private BoardMetaDto mapMeta;
    private String mapData;

    @JsonCreator
    public BoardResponse() {

    }
}
