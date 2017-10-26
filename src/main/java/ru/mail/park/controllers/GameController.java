package ru.mail.park.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.dto.MapMetaDto;
import ru.mail.park.dto.helpers.MapMetaHelper;
import ru.mail.park.services.GameDao;

import java.util.List;

@RestController
@RequestMapping("api")
public class GameController {
    private final GameDao gameDao;

    public GameController(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    @GetMapping("maps/get")
    public ResponseEntity<List<MapMetaDto>> getMaps(
            @PathVariable(value = "sort", required = false) String sort,
            @PathVariable(value = "page", required = false) Integer page
    ) {
        return ResponseEntity
                .ok(MapMetaHelper.toDto(
                        gameDao.getMaps(sort, page)
                ));
    }

    @PutMapping("maps/create")
    public ResponseEntity<MapMetaDto> createMap(@RequestBody MapMetaDto mapMetaDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MapMetaHelper.toDto(
                        gameDao.createMap(MapMetaHelper.fromDto(mapMetaDto))
                ));
    }
}
