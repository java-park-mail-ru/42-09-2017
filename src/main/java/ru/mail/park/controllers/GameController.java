package ru.mail.park.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.domain.dto.MapRequest;
import ru.mail.park.domain.dto.MapMetaDto;
import ru.mail.park.domain.dto.helpers.MapMetaHelper;
import ru.mail.park.services.GameDao;

import java.util.List;

@CrossOrigin(origins = {
        "https://sand42box.herokuapp.com",
        "https://nightly-42.herokuapp.com",
        "https://master-42.herokuapp.com"
})
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
    public ResponseEntity<MapMetaDto> createMap(@RequestBody MapRequest mapRequest) throws JsonProcessingException {
        MapMetaDto mapMetaDto = MapMetaHelper.toDto(
                gameDao.createMap(
                        mapRequest.getMapData(),
                        MapMetaHelper.fromDto(mapRequest.getMapMeta()))
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapMetaDto);
    }

    @GetMapping(value = "maps/{id}/get", produces = "application/json")
    public ResponseEntity<String> getMap(@PathVariable Integer id) {
        return ResponseEntity
                .ok(gameDao.getMap(id));
    }
}
