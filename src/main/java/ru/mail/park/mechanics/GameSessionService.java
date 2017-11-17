package ru.mail.park.mechanics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.BoardMessage;
import ru.mail.park.websocket.message.MovingMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    private Map<Id<User>, GameSession> gameSessionMap = new ConcurrentHashMap<>();
    private final GameDao gameDao;
    private final UserDao userDao;
    private final RemotePointService remotePointService;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(GameSessionService.class);

    public GameSessionService(
            GameDao gameDao,
            UserDao userDao,
            RemotePointService remotePointService
    ) {
        this.gameDao = gameDao;
        this.userDao = userDao;
        this.remotePointService = remotePointService;
    }

    public boolean isPlaying(Id<User> userId) {
        return gameSessionMap.containsKey(userId);
    }

    public void startGame(Id<User> first, Id<User> second, Id<Board> boardId) {
        GameSession gameSession = new GameSession(first, second, boardId);
        gameSessionMap.put(first, gameSession);
        gameSessionMap.put(second, gameSession);
        BoardRequest.Data board = gameDao.getBoard(boardId.getId());
        BoardMessage boardMessage = new BoardMessage(board);
        try {
            remotePointService.sendMessageTo(first, boardMessage);
        } catch (IOException ignore) {
            LOGGER.warn("Can't send message to first player with nickname "
                    + userDao.findUserById(first.getId()).getUsername()
            );
        }

        try {
            remotePointService.sendMessageTo(second, boardMessage);
        } catch (IOException e) {
            LOGGER.warn("Can't send message to second player with nickname "
                    + userDao.findUserById(second.getId()).getUsername()
            );
        }
    }

    public void finishGame(Id<User> first, Id<User> second) {
        gameSessionMap.remove(first);
        gameSessionMap.remove(second);
    }

    public void removeSessionFor(Id<User> userId) {
        GameSession gameSession = gameSessionMap.get(userId);
        if (gameSession == null) {
            return;
        }
        for (Id<User> user : gameSession.getPlayers()) {
            LOGGER.warn("Removing game session for user");
            gameSessionMap.remove(user);
            try {
                remotePointService.sendRowMessageTo(user, "You are kicked from game");
            } catch (IOException e) {
                LOGGER.warn("Can't send message");
            }
        }
    }

    public void sendSnapFrom(Id<User> userId, BodyFrame snap) {
        LOGGER.info("Trying to send snapshot from "
                + userDao.findUserById(userId.getId()).getUsername()
        );
        if (!isPlaying(userId)) {
            LOGGER.warn("I will not send snapshot because you are not playing");
            return;
        }
        GameSession gameSession = gameSessionMap.get(userId);
        gameSession.getPlayers().stream()
                .filter(id -> !userId.equals(id))
                .forEach(id -> {
                    try {
                        remotePointService.sendMessageTo(id, new MovingMessage(snap));
                    } catch (IOException ignore) {
                    }
                });

    }
}
