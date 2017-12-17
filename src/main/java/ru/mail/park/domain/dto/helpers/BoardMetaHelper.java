package ru.mail.park.domain.dto.helpers;

import ru.mail.park.domain.BoardMeta;
import ru.mail.park.domain.dto.BoardMetaDto;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BoardMetaHelper {
    public static BoardMetaDto toDto(BoardMeta boardMeta) {
        if (boardMeta == null) {
            return null;
        }
        final BoardMetaDto boardMetaDto = new BoardMetaDto();
        boardMetaDto.setId(boardMeta.getId());
        boardMetaDto.setMapId(boardMeta.getBoard().getId());
        boardMetaDto.setName(boardMeta.getName());
        boardMetaDto.setLevel(boardMeta.getLevel());
        boardMetaDto.setTimer(boardMeta.getTimer());
        boardMetaDto.setRating(boardMeta.getRating());
        boardMetaDto.setCreated(boardMeta.getCreated());
        boardMetaDto.setPreview(boardMeta.getPreview());
        boardMetaDto.setPlayedTimes(boardMeta.getPlayedTimes());
        boardMetaDto.setPlayers(boardMeta.getPlayers());
        return boardMetaDto;
    }

    public static List<BoardMetaDto> toDto(List<BoardMeta> boardMetaList) {
        final List<BoardMetaDto> boardMetaDtos = new ArrayList<>();
        for (BoardMeta boardMeta : boardMetaList) {
            boardMetaDtos.add(toDto(boardMeta));
        }
        return boardMetaDtos;
    }

    public static BoardMeta fromDto(BoardMetaDto boardMetaDto) {
        if (boardMetaDto == null) {
            return null;
        }
        final BoardMeta boardMeta = new BoardMeta();
        boardMeta.setName(boardMetaDto.getName());
        boardMeta.setLevel(boardMetaDto.getLevel());
        boardMeta.setTimer(boardMetaDto.getTimer());
        if (boardMetaDto.getRating() == null) {
            boardMeta.setRating(0);
        } else {
            boardMeta.setRating(boardMetaDto.getRating());
        }
        if (boardMetaDto.getCreated() == null) {
            boardMeta.setCreated(Date.valueOf(LocalDate.now()));
        } else {
            boardMeta.setCreated(boardMetaDto.getCreated());
        }
        boardMeta.setPreview(boardMetaDto.getPreview());
        if (boardMetaDto.getPlayedTimes() == null) {
            boardMeta.setPlayedTimes(0);
        } else {
            boardMeta.setPlayedTimes(boardMetaDto.getPlayedTimes());
        }
        if (boardMetaDto.getPlayers() == null) {
            boardMeta.setPlayers(1);
        } else {
            boardMeta.setPlayers(boardMetaDto.getPlayers());
        }
        return boardMeta;
    }

    public static List<BoardMeta> fromDto(List<BoardMetaDto> boardMetaDtoList) {
        final List<BoardMeta> boardMetas = new ArrayList<>();
        for (BoardMetaDto boardMetaDto : boardMetaDtoList) {
            boardMetas.add(fromDto(boardMetaDto));
        }
        return boardMetas;
    }
}
