package ru.mail.park.dto.helpers;

import ru.mail.park.controllers.domain.MapMeta;
import ru.mail.park.dto.MapMetaDto;

import java.util.ArrayList;
import java.util.List;

public class MapMetaHelper {
    public static MapMetaDto toDto(MapMeta mapMeta) {
        MapMetaDto mapMetaDto = new MapMetaDto(
                mapMeta.getName(),
                mapMeta.getLevel(),
                mapMeta.getTimer(),
                mapMeta.getCreated(),
                mapMeta.getPreview(),
                mapMeta.getPlayedTimes(),
                mapMeta.getPlayers()
        );
        mapMetaDto.setId(mapMeta.getId());
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
        MapMeta mapMeta =  new MapMeta(
                mapMetaDto.getName(),
                mapMetaDto.getLevel(),
                mapMetaDto.getTimer(),
                mapMetaDto.getCreated(),
                mapMetaDto.getPreview(),
                mapMetaDto.getPlayedTimes(),
                mapMetaDto.getPlayers()
        );
        mapMeta.setId(mapMetaDto.getId());
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
