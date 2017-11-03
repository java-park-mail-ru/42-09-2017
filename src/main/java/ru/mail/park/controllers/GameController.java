package ru.mail.park.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.domain.dto.BoardMetaDto;
import ru.mail.park.domain.dto.helpers.BoardMetaHelper;
import ru.mail.park.services.GameDao;

import java.util.List;

@CrossOrigin(origins = {
        "https://sand42box.herokuapp.com",
        "https://nightly-42.herokuapp.com",
        "https://master-42.herokuapp.com",
        "http://localhost",
        "http://127.0.0.1"
})
@RestController
@RequestMapping(value = "api/game", produces = "application/json")
public class GameController {
    private final GameDao gameDao;

    public GameController(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    /* ToDo: 03.11.2017 Use sessions and not allow everybody to do it */

    @GetMapping("maps/get")
    public ResponseEntity<List<BoardMetaDto>> getMaps(
            @PathVariable(value = "sort", required = false) String sort,
            @PathVariable(value = "page", required = false) Integer page
    ) {
        return ResponseEntity
                .ok(BoardMetaHelper.toDto(
                        gameDao.getBoards(sort, page)
                ));
    }

    @PostMapping("map/{name}/create")
    public ResponseEntity<BoardMetaDto> createMap(
            @PathVariable String name, @RequestBody BoardRequest boardRequest
    ) throws JsonProcessingException {
        boardRequest.getBoardMetaDto().setName(name);
        BoardMetaDto boardMetaDto = BoardMetaHelper.toDto(
                gameDao.createBoard(
                        boardRequest.getBoardDataMap(),
                        BoardMetaHelper.fromDto(boardRequest.getBoardMetaDto()))
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(boardMetaDto);
    }

    @PutMapping("map/{id}")
    public ResponseEntity<BoardMetaDto> updateMap(
            @PathVariable Integer id, @RequestBody BoardRequest boardRequest
    ) {
        BoardMetaDto boardMetaDto = BoardMetaHelper.toDto(
                gameDao.updateBoard(
                        id,
                        boardRequest.getBoardDataMap(),
                        BoardMetaHelper.fromDto(boardRequest.getBoardMetaDto())
                )
        );

        return ResponseEntity
                .ok(boardMetaDto);
    }

    @GetMapping("map/{id}")
    public ResponseEntity<String> getMap(@PathVariable Integer id) {
        return ResponseEntity
                .ok(gameDao.getBoard(id));
    }
}
