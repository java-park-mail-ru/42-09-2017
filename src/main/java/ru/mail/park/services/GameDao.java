package ru.mail.park.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.BoardMeta;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.exceptions.ControllerValidationException;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.mechanics.WorldParser;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.mail.park.info.constants.Constants.MAPS_ON_PAGE;

@Service
@Transactional
public class GameDao {
    private EntityManager em;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GameDao.class);
    private List<WorldParser> worldParsers = new ArrayList<>();

    public GameDao(EntityManager em) {
        this.em = em;
    }

    public void registerParser(WorldParser worldParser) {
        worldParsers.add(worldParser);
    }

    public WorldParser getLastParser() {
        return worldParsers.get(worldParsers.size() - 1);
    }

    public List<BoardMeta> getMetas(@Nullable String sort, @Nullable Integer page) {
        StringBuilder sql = new StringBuilder("select m from BoardMeta m");
        if (sort != null) {
            sql.append(" order by ").append(sort);
        }
        if (page != null) {
            return getMetasPaged(sql, page);
        }
        try {
            return em.createQuery(sql.toString(), BoardMeta.class)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    private List<BoardMeta> getMetasPaged(StringBuilder sql, Integer page) {
        sql.append(" limit :limit offset :offset");
        try {
            return em.createQuery(sql.toString(), BoardMeta.class)
                    .setParameter("limit", MAPS_ON_PAGE)
                    .setParameter("offset", MAPS_ON_PAGE * (page - 1))
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    public String getBoardString(Long id) {
        Board board = em.find(Board.class, id);
        LOGGER.info("Found board with id " + id.toString());
        if (board == null) {
            return null;
        }
        return board.getData();
    }

    public BoardRequest.Data getBoard(Long id) {
        String boardString = getBoardString(id);
        if (boardString == null) {
            return null;
        }
        try {
            return MAPPER.readValue(boardString, BoardRequest.Data.class);
        } catch (IOException e) {
            return null;
        }
    }

    public BoardMeta getMetaOf(Long boardId) {
        try {
            return em.createQuery("select m from BoardMeta m where board_id = :board", BoardMeta.class)
                    .setParameter("board", boardId)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.error("Can't find meta");
            return null;
        }
    }

    private boolean hasName(String name) {
        try {
            Long count = em.createQuery("select count(id) from BoardMeta where name = :name", Long.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return count != 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public BoardMeta createBoard(BoardRequest.Data boardData, BoardMeta boardMeta) throws JsonProcessingException {
        if (hasName(boardMeta.getName())) {
            List<String> errors = new ArrayList<>();
            errors.add(MessageConstants.BOARD_EXISTS);
            throw new ControllerValidationException(errors);
        }
        Board board = new Board();
        LOGGER.warn(MAPPER.writeValueAsString(boardData.getBodies()));
        LOGGER.warn(MAPPER.writeValueAsString(boardData.getJoints()));
        board.setData(MAPPER.writeValueAsString(boardData));
        em.persist(board);
        boardMeta.setBoard(board);
        em.persist(boardMeta);
        LOGGER.info("Board created with id " + board.getId().toString());
        return boardMeta;
    }

    public BoardMeta updateBoard(Long id, BoardRequest.Data boardData, BoardMeta boardMeta) {
        Board board = em.find(Board.class, id);
        if (board == null) {
            /* ToDo: 03.11.2017 Throw NotFoundException */
            return null;
        }
        try {
            if (boardData != null) {
                board.setData(MAPPER.writeValueAsString(boardData));
            }
            if (boardMeta != null) {
                updateBoardMeta(board.getMeta(), boardMeta);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return board.getMeta();
    }

    private void updateBoardMeta(BoardMeta boardMeta, BoardMeta metaNew) {
        if (metaNew.getName() != null) {
            boardMeta.setName(metaNew.getName());
        }
        if (metaNew.getTimer() != null) {
            boardMeta.setTimer(metaNew.getTimer());
        }
        if (metaNew.getLevel() != null) {
            boardMeta.setLevel(metaNew.getLevel());
        }
        if (metaNew.getPlayers() != null) {
            boardMeta.setPlayers(metaNew.getPlayers());
        }
        if (metaNew.getPreview() != null) {
            boardMeta.setPreview(metaNew.getPreview());
        }
    }
}
