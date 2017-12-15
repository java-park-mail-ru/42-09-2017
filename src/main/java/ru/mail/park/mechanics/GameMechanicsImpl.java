package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.BoardMeta;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.from.MovingMessage;
import ru.mail.park.websocket.message.from.SnapMessage;
import ru.mail.park.websocket.message.to.BoardMessage;
import ru.mail.park.websocket.message.to.FinishedMessage;
import ru.mail.park.websocket.message.to.StartedMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.mail.park.info.constants.MessageConstants.GAME_ERROR;

@Service
@Scope("prototype")
public class GameMechanicsImpl implements GameMechanics {
    private Id<GameMechanics> id;
    private final UserDao userDao;
    private final GameDao gameDao;
    private final RemotePointService remotePointService;
    private final GameSessionService gameSessionService;
    private final WorldRunnerService worldRunnerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMechanics.class);

    private Map<Id<Board>, Set<Id<User>>> boardUserMap = new ConcurrentHashMap<>();
    private Map<Id<Board>, Integer> boardCapacityMap = new ConcurrentHashMap<>();
    private Map<Id<User>, Id<Board>> userBoardMap = new ConcurrentHashMap<>();
    private Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public GameMechanicsImpl(
            UserDao userDao,
            GameDao gameDao,
            RemotePointService remotePointService,
            GameSessionService gameSessionService,
            WorldRunnerService worldRunnerService
    ) {
        this.userDao = userDao;
        this.gameDao = gameDao;
        this.remotePointService = remotePointService;
        this.gameSessionService = gameSessionService;
        this.worldRunnerService = worldRunnerService;
    }

    public Id<GameMechanics> getId() {
        return id;
    }

    public void setId(int mechanicsId) {
        id = Id.of(mechanicsId);
    }

    public void gameStep() {
        while (!tasks.isEmpty()) {
            final Runnable nextTask = tasks.poll();
            if (nextTask != null) {
                try {
                    nextTask.run();
                } catch (RuntimeException ex) {
                    LOGGER.error("Can't handle game task", ex);
                }
            }
        }
        tryFinishGame();
        processFinishedSimulation();
        tryStartSimulation();
        tryJoinGame();
    }

    public boolean addWaiter(Id<User> userId, Id<Board> board) {
        if (gameSessionService.isPlaying(userId)) {
            LOGGER.warn("Player is in game now");
            return false;
        }
        Id<Board> found = userBoardMap.get(userId);
        if (found != null) {
            if (found.equals(board)) {
                LOGGER.warn("Already subscribed on this board");
                return false;
            } else {
                LOGGER.warn("Resubscribing on another board");
                boardUserMap.get(found)
                        .remove(userId);
            }
        }
        BoardMeta meta = gameDao.getMetaOf(board.getId());
        if (meta == null) {
            LOGGER.error("Subscribed on bad board. Closing session");
            remotePointService.cutDownConnection(userId, CloseStatus.SERVER_ERROR);
            return false;
        }
        boardUserMap.putIfAbsent(board, new LinkedHashSet<>());
        boardCapacityMap.putIfAbsent(board, meta.getPlayers());
        boardUserMap.get(board)
                .add(userId);
        userBoardMap.put(userId, board);
        LOGGER.info("User added in queue");
        return true;
    }

    public void addBoardMessageTask(Set<Id<User>> players) {
        LOGGER.info("Team is found");
        BoardMessage message = new BoardMessage();
        final long[] playerId = {1};
        players.forEach(player -> tasks.add(() -> {
            LOGGER.info("Sending board message");
            try {
                message.setPlayerID(playerId[0]);
                remotePointService.sendMessageTo(player, message);
                gameSessionService.setMovingForSession(player);
                playerId[0]++;
            } catch (IOException e) {
                LOGGER.warn("Can't send board message to player " + playerId[0]);
            }

        }));
    }

    public void addMovingMessageTask(Id<User> from, BodyFrame snap) {
        Set<Id<User>> players = gameSessionService.getTeamOf(from);
        MovingMessage message = new MovingMessage(snap);
        players.stream()
                .filter(playerId -> !from.equals(playerId))
                .forEach(playerId -> tasks.add(() -> {
                    LOGGER.info("Sending moving message from " + userDao.findUserById(from.getId()).getUsername());
                    try {
                        remotePointService.sendMessageTo(playerId, message);
                    } catch (IOException e) {
                        LOGGER.error("Can't send moving snap");
                    }
                }));
    }

    public void addStartedMessageTask(GameSession session) {
        StartedMessage message = new StartedMessage();
        session.getPlayers().stream()
                .filter(Objects::nonNull)
                .forEach(playerId -> tasks.add(() -> {
                    try {
                        remotePointService.sendMessageTo(playerId, message);
                    } catch (IOException e) {
                        LOGGER.warn("Error with sending started message");
                    }
                }));
    }

    public void addSnapMessageTask(Id<User> userId, SnapMessage message) {
        tasks.add(() -> {
            try {
                remotePointService.sendMessageTo(userId, message);
            } catch (IOException e) {
                LOGGER.error("Can't send difference snap");
            }
        });
    }

    // ToDo: 01.12.17  Try second way: checking all finished players in gmStep() with tryFinishGame()
    public void addFinishedMessageTask(Id<User> userId, FinishedMessage message) {
        tasks.add(() -> {
            try {
                remotePointService.sendMessageTo(userId, message);
            } catch (IOException e) {
                LOGGER.error("Can't send finish message");
            }
            removeWaiter(userId);
            // ToDo: 02.12.17  These calls can be removed from this method (may be)
            gameSessionService.removeSessionFor(userId);
        });
    }

    private Set<Id<User>> matchPlayers(Set<Id<User>> waiters, int players) {
        Set<Id<User>> matchedPlayers = new LinkedHashSet<>();
        Iterator<Id<User>> waitersIterator = waiters.iterator();
        int count = 0;
        while (count < players && waitersIterator.hasNext()) {
            Id<User> waiter = waitersIterator.next();
            if (!checkCandidate(waiter)) {
                waitersIterator.remove();
                continue;
            }
            matchedPlayers.add(waiter);
            count++;
        }
        if (count < players) {
            return null;
        }
        waiters.removeAll(matchedPlayers);
        matchedPlayers.forEach(userBoardMap::remove);
        return matchedPlayers;
    }

    public void tryJoinGame() {
        boardUserMap.forEach((boardId, waiters) -> {
            int players = boardCapacityMap.get(boardId);
            Set<Id<User>> matchedPlayers;
            while (waiters.size() / players > 0) {
                matchedPlayers = matchPlayers(waiters, players);
                if (matchedPlayers != null) {
                    gameSessionService.joinGame(id, boardId, matchedPlayers);
                    addBoardMessageTask(matchedPlayers);
                }
            }
        });
    }

    public void tryStartSimulation() {
        gameSessionService.getSessionsByMechanicsId(id).stream()
                .filter(GameSession::isReady)
                .forEach(session -> {
                    session.setState(GameState.SIMULATION);
                    worldRunnerService.initAndRun(session);
                });
    }

    public void processFinishedSimulation() {
        gameSessionService.getSessionsByMechanicsId(id).stream()
                .filter(GameSession::isSimulated)
                .forEach(session -> {
                    session.setState(GameState.HANDLING);
                    addStartedMessageTask(session);
                });
    }

    public void tryFinishGame() {
        gameSessionService.getSessionsByMechanicsId(id).stream()
                .filter(GameSession::isFinished)
                .forEach(session -> gameSessionService.removeSessionForTeam(id, session));
    }

    private boolean checkCandidate(Id<User> userId) {
        return remotePointService.isConnected(userId)
                && !gameSessionService.isPlaying(userId)
                && userDao.findUserById(userId.getId()) != null;
    }

    public void removeWaiter(Id<User> userId) {
        LOGGER.warn("Removing board waiter");
        Id<Board> toRemove = userBoardMap.remove(userId);
        if (toRemove != null) {
            boardUserMap.get(toRemove)
                    .remove(userId);
        }
    }

    public void userDisconnected(Id<User> userId) {
        removeWaiter(userId);
        GameSession session = gameSessionService.getSessionFor(userId);
        if (session == null) {
            LOGGER.warn("User disconnected. GameSession was null");
            return;
        }
        if (session.isMoving() || session.isReady()) {
            FinishedMessage message = new FinishedMessage(0L, GAME_ERROR);
            session.getPlayers().forEach(player -> addFinishedMessageTask(player, message));
            gameSessionService.removeSessionForTeam(id, userId);
        } else {
            gameSessionService.removeSessionFor(userId);
        }
    }
}
