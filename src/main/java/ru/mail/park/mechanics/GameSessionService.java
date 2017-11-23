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
import ru.mail.park.mechanics.objects.body.BodyData;
import ru.mail.park.mechanics.objects.body.GBody;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.to.BoardMessage;
import ru.mail.park.websocket.message.from.MovingMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    private Map<Id<User>, GameSession> gameSessionMap = new ConcurrentHashMap<>();
    private Map<Id<User>, Player> playerMap = new ConcurrentHashMap<>();
    private final GameDao gameDao;
    private final UserDao userDao;
    private final RemotePointService remotePointService;
    private final WorldRunnerService worldRunnerService;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(GameSessionService.class);

    public GameSessionService(
            GameDao gameDao,
            UserDao userDao,
            RemotePointService remotePointService,
            WorldRunnerService worldRunnerService
    ) {
        this.gameDao = gameDao;
        this.userDao = userDao;
        this.remotePointService = remotePointService;
        this.worldRunnerService = worldRunnerService;
    }

    public boolean isPlaying(Id<User> userId) {
        return gameSessionMap.containsKey(userId);
    }

    public boolean isSimulationStartedFor(Id<User> userId) {
        GameSession gameSession = gameSessionMap.get(userId);
        return gameSession == null || gameSession.isSimulating();
    }

    public boolean isTeamReady(Id<User> userId) {
        GameSession gameSession = gameSessionMap.get(userId);
        long notReadyCount = gameSession.getPlayers().stream()
                .filter(id -> !playerMap.get(id).isReady())
                .count();
        return notReadyCount == 0;
    }

    public GameSession getSessionFor(Id<User> userId) {
        return gameSessionMap.get(userId);
    }

    public void startSimulation(GameSession gameSession) {
        BoardRequest.Data board = gameDao.getBoard(gameSession.getBoardId().getId());
        Map<Long, GBody> bodiesMap = new HashMap<>();
        board.getBodies().forEach(body -> bodiesMap.put(body.getId(), body));
        LOGGER.info("Bodies: " + bodiesMap.size());
        Map<Id<User>, List<BodyFrame>> initSnapsMap = gameSession.getInitSnapsMap();
        LOGGER.info("Init snaps map size: " + initSnapsMap.size());
        for (Map.Entry<Id<User>, List<BodyFrame>> initSnap : initSnapsMap.entrySet()) {
            LOGGER.info("   value size: " + initSnap.getValue().size());
            initSnap.getValue().forEach(bodyFrame -> {
                LOGGER.info("   body frame id: " + bodyFrame.getId());
                BodyData bodyData = bodiesMap.get(bodyFrame.getId()).getData();
                bodyData.setPosition(bodyFrame.getPosition());
                bodyData.setAngle(bodyFrame.getAngle());
            });
        }
        worldRunnerService.initWorld(gameSession, board.getBodies(), board.getJoints());
        worldRunnerService.runSimulation(gameSession);
    }

    public void startGame(Id<User> first, Id<User> second, Id<Board> boardId) {
        GameSession gameSession = new GameSession(first, second, boardId);
        gameSessionMap.put(first, gameSession);
        playerMap.put(first, new Player(userDao.findUserById(first.getId())));
        BoardMessage boardMessage = new BoardMessage();
        try {
            remotePointService.sendMessageTo(first, boardMessage);
        } catch (IOException ignore) {
            LOGGER.warn("Can't send message to first player with nickname "
                    + userDao.findUserById(first.getId()).getUsername()
            );
        }

        if (second != null) {
            gameSessionMap.put(second, gameSession);
            playerMap.put(second, new Player(userDao.findUserById(second.getId())));
            try {
                boardMessage.setPlayerID(2L);
                remotePointService.sendMessageTo(second, boardMessage);
            } catch (IOException e) {
                LOGGER.warn("Can't send message to second player with nickname "
                        + userDao.findUserById(second.getId()).getUsername()
                );
            }
        }
    }

    public void setReady(Id<User> userId) {
        playerMap.get(userId).setReady(true);
    }

    public void finishGame(Id<User> first, Id<User> second) {
        gameSessionMap.remove(first);
        playerMap.remove(first);
        try {
            gameSessionMap.remove(second);
            playerMap.remove(second);
        } catch (NullPointerException e) {
            LOGGER.warn("Session removed only for first player, because it's single player");
        }
    }

    public void removeSessionFor(Id<User> userId) {
        GameSession gameSession = gameSessionMap.get(userId);
        if (gameSession == null) {
            return;
        }
        gameSessionMap.remove(userId);
        playerMap.remove(userId);
        try {
            remotePointService.sendRowMessageTo(userId, "You are kicked from game");
        } catch (IOException e) {
            LOGGER.warn("Can't send message");
        }
    }

    public void removeSessionForTeam(Id<User> userId) {
        GameSession gameSession = gameSessionMap.get(userId);
        if (gameSession == null) {
            return;
        }
        for (Id<User> user : gameSession.getPlayers()) {
            if (user == null) {
                continue;
            }
            LOGGER.warn("Removing game session for user");
            gameSessionMap.remove(user);
            playerMap.remove(user);
        }
        worldRunnerService.removeWorldRunnerFor(gameSession);
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
                    } catch (IOException e) {
                        LOGGER.error("Can't send moving snap");
                    }
                });

    }
}
