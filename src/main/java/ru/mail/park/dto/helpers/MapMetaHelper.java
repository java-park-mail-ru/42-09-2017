package ru.mail.park.dto.helpers;

import ru.mail.park.controllers.domain.MapMeta;
import ru.mail.park.dto.MapMetaDto;

import java.util.ArrayList;
import java.util.List;

public class MapMetaHelper {
    public static MapMetaDto toDto(MapMeta mapMeta) {
        return new MapMetaDto(
                mapMeta.getId(),
                mapMeta.getName(),
                mapMeta.getLevel(),
                mapMeta.getTimer(),
                mapMeta.getCreated(),
                mapMeta.getPreview(),
                mapMeta.getPlayedTimes(),
                mapMeta.getPlayers()
        );
    }

    public static List<MapMetaDto> toDto(List<MapMeta> mapMetaList) {
        List<MapMetaDto> mapList = new ArrayList<>();
        for (MapMeta mapMeta : mapMetaList) {
            mapList.add(toDto(mapMeta));
        }
        return mapList;
    }

    public static MapMeta fromDto(MapMetaDto mapMetaDto) {
        return new MapMeta(
                mapMetaDto.getId(),
                mapMetaDto.getName(),
                mapMetaDto.getLevel(),
                mapMetaDto.getTimer(),
                mapMetaDto.getCreated(),
                mapMetaDto.getPreview(),
                mapMetaDto.getPlayedTimes(),
                mapMetaDto.getPlayers()
        );
    }

    public static List<MapMeta> fromDto(List<MapMetaDto> mapMetaDtoList) {
        List<MapMeta> mapList = new ArrayList<>();
        for (MapMetaDto mapMetaDto : mapMetaDtoList) {
            mapList.add(fromDto(mapMetaDto));
        }
        return mapList;
    }
}
