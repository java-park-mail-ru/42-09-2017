package ru.mail.park.mechanics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.services.GameDao;
import ru.mail.park.websocket.message.BoardMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    private Map<Id<User>, GameSession> gameSessionMap = new ConcurrentHashMap<>();
    private final GameDao gameDao;
    private final RemotePointService remotePointService;
    private final ObjectMapper mapper = new ObjectMapper();

    public GameSessionService(
            GameDao gameDao,
            RemotePointService remotePointService
    ) {
        this.gameDao = gameDao;
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
        try {
            remotePointService.sendMessageTo(first, new BoardMessage(board));
        } catch (IOException e) {

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
            gameSessionMap.remove(user);
        }
    }
}
