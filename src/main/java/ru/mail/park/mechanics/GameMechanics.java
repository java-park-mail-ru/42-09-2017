package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.BoardMeta;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.mechanics.objects.body.BodyData;
import ru.mail.park.mechanics.objects.body.GBody;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.mail.park.info.constants.Constants.POOL_SIZE;
import static ru.mail.park.info.constants.MessageConstants.GAME_ERROR;
import static ru.mail.park.info.constants.MessageConstants.SUCCESS;

@Service
public class GameMechanics {
    private final UserDao userDao;
    private final GameDao gameDao;
    private final RemotePointService remotePointService;
    private final GameSessionService gameSessionService;
    private final WorldRunnerService worldRunnerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMechanics.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);

    private Map<Id<Board>, Set<Id<User>>> boardUserMap = new ConcurrentHashMap<>();
    private Map<Id<Board>, Integer> boardCapacityMap = new ConcurrentHashMap<>();
    private Map<Id<User>, Id<Board>> userBoardMap = new ConcurrentHashMap<>();
    private Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public GameMechanics(
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

    public void addWaiter(Id<User> userId, Id<Board> board) {
        if (gameSessionService.isPlaying(userId)) {
            LOGGER.warn("Player is in game now");
            return;
        }
        Id<Board> found = userBoardMap.get(userId);
        if (found != null) {
            if (found.equals(board)) {
                LOGGER.warn("Already subscribed on this board");
                return;
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
            return;
        }
        boardUserMap.putIfAbsent(board, new LinkedHashSet<>());
        boardCapacityMap.putIfAbsent(board, meta.getPlayers());
        boardUserMap.get(board)
                .add(userId);
        userBoardMap.put(userId, board);
        LOGGER.info("User added in queue");
    }

    private void addBoardMessageTask(Set<Id<User>> players) {
        LOGGER.info("Team is found");
        BoardMessage message = new BoardMessage();
        final long[] id = {1};
        players.forEach(player -> tasks.add(() -> {
            LOGGER.info("Sending board message");
            try {
                message.setPlayerID(id[0]);
                remotePointService.sendMessageTo(player, message);
                gameSessionService.setMovingForSession(player);
                id[0]++;
            } catch (IOException e) {
                LOGGER.warn("Can't send board message to player " + id[0]);
            }

        }));
    }

    private void addMovingMessageTask(Id<User> from, BodyFrame snap) {
        Set<Id<User>> players = gameSessionService.getTeamOf(from);
        MovingMessage message = new MovingMessage(snap);
        players.stream()
                .filter(id -> !from.equals(id))
                .forEach(id -> tasks.add(() -> {
                    LOGGER.info("Sending moving message from " + userDao.findUserById(from.getId()).getUsername());
                    try {
                        remotePointService.sendMessageTo(id, message);
                    } catch (IOException e) {
                        LOGGER.error("Can't send moving snap");
                    }
                }));
    }

    private void addStartedMessageTask(GameSession session) {
        StartedMessage message = new StartedMessage();
        session.getPlayers().stream()
                .filter(Objects::nonNull)
                .forEach(id -> tasks.add(() -> {
                    try {
                        remotePointService.sendMessageTo(id, message);
                    } catch (IOException e) {
                        LOGGER.warn("Error with sending started message");
                    }
                }));
    }

    private void addSnapMessageTask(Id<User> userId, SnapMessage message) {
        tasks.add(() -> {
            try {
                remotePointService.sendMessageTo(userId, message);
            } catch (IOException e) {
                LOGGER.error("Can't send difference snap");
            }
        });
    }

    // ToDo: 01.12.17  Try second way: checking all finished players in gmStep() with tryFinishGame()
    private void addFinishedMessageTask(Id<User> userId, FinishedMessage message) {
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

    private void tryJoinGame() {
        boardUserMap.forEach((boardId, waiters) -> {
            int players = boardCapacityMap.get(boardId);
            Set<Id<User>> matchedPlayers;
            while (waiters.size() / players > 0) {
                matchedPlayers = matchPlayers(waiters, players);
                if (matchedPlayers != null) {
                    gameSessionService.joinGame(boardId, matchedPlayers);
                    addBoardMessageTask(matchedPlayers);
                }
            }
        });
    }

    private void tryStartSimulation() {
        gameSessionService.getSessions().stream()
                .filter(GameSession::isReady)
                .forEach(session -> {
                    session.setState(GameState.SIMULATION);
                    executorService.submit(() -> {
                        LOGGER.warn("Starting simulation in new thread");
                        BoardRequest.Data board = gameDao.getBoard(session.getBoardId().getId());
                        Map<Long, GBody> bodiesMap = new HashMap<>();
                        board.getBodies().forEach(body -> bodiesMap.put(body.getId(), body));
                        LOGGER.info("Bodies: " + bodiesMap.size());
                        Map<Id<User>, List<BodyFrame>> initSnapsMap = session.getInitSnapsMap();
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
                        worldRunnerService.initWorld(session, board);
                        worldRunnerService.runSimulation(session);
                    });
                });
    }

    private void processFinishedSimulation() {
        gameSessionService.getSessions().stream()
                .filter(GameSession::isSimulated)
                .forEach(session -> {
                    session.setState(GameState.HANDLING);
                    executorService.submit(() -> addStartedMessageTask(session));
                });
    }

    private void tryFinishGame() {
        gameSessionService.getSessions().stream()
                .filter(GameSession::isFinished)
                .forEach(gameSessionService::removeSessionForTeam);
    }

    public void handleMoving(Id<User> userId, BodyFrame snap) {
        LOGGER.info("Handle moving");
        if (!gameSessionService.isPlaying(userId) || !gameSessionService.isMovingState(userId)) {
            LOGGER.warn("I will not send snapshot because you are not playing "
                    + "or session is not in Moving state");
            return;
        }
        addMovingMessageTask(userId, snap);
    }

    public void handleStart(Id<User> userId, List<BodyFrame> snap) {
        LOGGER.info("Handle start");
        gameSessionService.prepareSimulation(userId, snap);
    }

    public void handleSnap(Id<User> userId, SnapMessage snap) throws NullPointerException {
        LOGGER.info("Handle snap");
        GameSession session = gameSessionService.getSessionFor(userId);
        if (session == null) {
            LOGGER.error("Can't handle snap. Session is null");
        }
        boolean cheat = worldRunnerService.handleSnap(session, snap);
        if (cheat) {
            addSnapMessageTask(userId, snap);
        }
    }

    public void handleFinish(Id<User> userId) {
        LOGGER.info("Handle finish");
        gameSessionService.setFinishedForPlayer(userId);
        addFinishedMessageTask(userId, new FinishedMessage(1L, SUCCESS));
        if (gameSessionService.isTeamFinished(userId)) {
            gameSessionService.setFinishedForSession(userId);
        }
    }

    private boolean checkCandidate(Id<User> userId) {
        return remotePointService.isConnected(userId)
                && !gameSessionService.isPlaying(userId)
                && userDao.findUserById(userId.getId()) != null;
    }

    private void removeWaiter(Id<User> userId) {
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
            gameSessionService.removeSessionForTeam(userId);
        } else {
            gameSessionService.removeSessionFor(userId);
        }
    }
}
