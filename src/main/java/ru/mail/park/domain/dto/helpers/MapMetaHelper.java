package ru.mail.park.domain.dto.helpers;

import ru.mail.park.domain.MapMeta;
import ru.mail.park.domain.dto.MapMetaDto;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MapMetaHelper {
    public static MapMetaDto toDto(MapMeta mapMeta) {
        MapMetaDto mapMetaDto = new MapMetaDto();
        mapMetaDto.setId(mapMeta.getId());
        mapMetaDto.setMapId(mapMeta.getMap().getId());
        mapMetaDto.setName(mapMeta.getName());
        mapMetaDto.setLevel(mapMeta.getLevel());
        mapMetaDto.setTimer(mapMeta.getTimer());
        mapMetaDto.setRating(mapMeta.getRating());
        mapMetaDto.setCreated(mapMeta.getCreated());
        mapMetaDto.setPreview(mapMeta.getPreview());
        mapMetaDto.setPlayedTimes(mapMeta.getPlayedTimes());
        mapMetaDto.setPlayers(mapMeta.getPlayers());
        return mapMetaDto;
    }

    public static List<MapMetaDto> toDto(List<MapMeta> mapMetaList) {
        List<MapMetaDto> mapList = new ArrayList<>();
        for (MapMeta mapMeta : mapMetaList) {
            mapList.add(toDto(mapMeta));
        }
        return mapList;
    }

    public static MapMeta fromDto(MapMetaDto mapMetaDto) {
        MapMeta mapMeta = new MapMeta();
        mapMeta.setName(mapMetaDto.getName());
        mapMeta.setLevel(mapMetaDto.getLevel());
        mapMeta.setTimer(mapMetaDto.getTimer());
        if (mapMetaDto.getRating() == null) {
            mapMeta.setRating(0);
        } else {
            mapMeta.setRating(mapMetaDto.getRating());
        }
        if (mapMetaDto.getCreated() == null) {
            mapMeta.setCreated(Date.valueOf(LocalDate.now()));
        } else {
            mapMeta.setCreated(mapMetaDto.getCreated());
        }
        mapMeta.setPreview(mapMetaDto.getPreview());
        if (mapMetaDto.getPlayedTimes() == null) {
            mapMeta.setPlayedTimes(0);
        } else {
            mapMeta.setPlayedTimes(mapMetaDto.getPlayedTimes());
        }
        if (mapMetaDto.getPlayers() == null) {
            mapMeta.setPlayers(1);
        } else {
            mapMeta.setPlayers(mapMetaDto.getPlayers());
        }
        return mapMeta;
    }

    public static List<MapMeta> fromDto(List<MapMetaDto> mapMetaDtoList) {
        List<MapMeta> mapList = new ArrayList<>();
        for (MapMetaDto mapMetaDto : mapMetaDtoList) {
            mapList.add(fromDto(mapMetaDto));
        }
        return mapList;
    }
}
