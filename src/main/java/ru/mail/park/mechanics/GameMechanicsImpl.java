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
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.mechanics.domain.objects.BodyFrame;
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

    private final Map<Id<User>, Id<Board>> userBoardMap = new ConcurrentHashMap<>();
    private final Map<Id<Board>, Set<Id<User>>> boardUserMap = new ConcurrentHashMap<>();
    private final Map<Id<Board>, BoardMeta> boardMetaMap = new ConcurrentHashMap<>();
    private final Map<Id<Board>, BoardRequest.Data> boardMap = new ConcurrentHashMap<>();
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

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

    @Override
    public void gameStep() {
        try {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addWaiter(Id<User> userId, Id<Board> board) {
        if (gameSessionService.isPlaying(userId)) {
            LOGGER.warn("Player is in game now");
            return false;
        }
        final Id<Board> found = userBoardMap.get(userId);
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
        final boolean[] added = {true};
        boardMetaMap.computeIfAbsent(board, boardId -> {
            final BoardMeta meta = gameDao.getMetaOf(board.getId());
            if (meta == null) {
                LOGGER.error("Subscribed on bad board. Closing session");
                remotePointService.cutDownConnection(userId, CloseStatus.SERVER_ERROR);
                added[0] = false;
                return null;
            }
            return meta;
        });
        if (!added[0]) {
            return false;
        }
        boardUserMap.putIfAbsent(board, new LinkedHashSet<>());
        boardMap.computeIfAbsent(board, boardId -> gameDao.getBoard(boardId.getId()));
        boardUserMap.get(board)
                .add(userId);
        userBoardMap.put(userId, board);
        LOGGER.info("User added in queue");
        return true;
    }

    @Override
    public void addBoardMessageTask(Map<Id<User>, User> userMap) {
        LOGGER.info("Team is found");
        final BoardMessage message = new BoardMessage();
        final long[] playerId = {1};
        userMap.forEach((userId, user) -> tasks.add(() -> {
            LOGGER.info("Sending board message");
            try {
                final User[] friend = {null};
                userMap.forEach((friendId, friendObj) -> {
                    if (!userId.equals(friendId)) {
                        friend[0] = friendObj;
                    }
                });
                if (friend[0] == null) {
                    LOGGER.error("Friend is null");
                } else {
                    message.setFriend(friend[0].getUsername());
                    message.setLevel(friend[0].getLevel());
                    LOGGER.warn("  friend: " + friend[0].getUsername());
                }
                message.setPlayerID(playerId[0]);
                remotePointService.sendMessageTo(userId, message);
                gameSessionService.setPlayerId(userId, Id.of(playerId[0]));
                gameSessionService.setMovingForSession(userId);
                playerId[0]++;
            } catch (IOException e) {
                LOGGER.warn("Can't send board message to player " + playerId[0]);
            }

        }));
    }

    @Override
    public void addMovingMessageTask(Id<User> from, BodyFrame snap) {
        final Set<Id<User>> players = gameSessionService.getTeamOf(from);
        final MovingMessage message = new MovingMessage(snap);
        players.stream()
                .filter(playerId -> !from.equals(playerId))
                .forEach(playerId -> tasks.add(() -> {
                    LOGGER.info("Sending moving message");
                    try {
                        remotePointService.sendMessageTo(playerId, message);
                    } catch (IOException e) {
                        LOGGER.error("Can't send moving snap");
                    }
                }));
    }

    @Override
    public void addStartedMessageTask(GameSession session) {
        final StartedMessage message = new StartedMessage();
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

    @Override
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
    @Override
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
        final Set<Id<User>> matchedPlayers = new LinkedHashSet<>();
        final Iterator<Id<User>> waitersIterator = waiters.iterator();
        int count = 0;
        while (count < players && waitersIterator.hasNext()) {
            final Id<User> waiter = waitersIterator.next();
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

    @Override
    public void tryJoinGame() {
        boardUserMap.forEach((boardId, waiters) -> {
            final int players = boardMetaMap.get(boardId).getPlayers();
            while (waiters.size() / players > 0) {
                final Set<Id<User>> matchedPlayers = matchPlayers(waiters, players);
                if (matchedPlayers != null) {
                    final BoardRequest.Data board = boardMap.get(boardId);
                    final Map<Id<User>, User> users = new HashMap<>();
                    matchedPlayers.forEach(userId -> users.put(userId, userDao.findUserById(userId.getId())));
                    gameSessionService.joinGame(id, boardId, board, users);
                    addBoardMessageTask(users);
                }
            }
        });
    }

    @Override
    public void tryStartSimulation() {
        gameSessionService.getSessionsByMechanicsId(id).stream()
                .filter(GameSession::isReady)
                .forEach(session -> {
                    session.setState(GameState.SIMULATION);
                    final BoardMeta meta = boardMetaMap.get(session.getBoardId());
                    worldRunnerService.initAndRun(session, meta.getTimer());
                });
    }

    @Override
    public void processFinishedSimulation() {
        gameSessionService.getSessionsByMechanicsId(id).stream()
                .filter(GameSession::isSimulated)
                .forEach(session -> {
                    gameSessionService.setScores(session);
                    session.setState(GameState.HANDLING);
                    addStartedMessageTask(session);
                });
    }

    @Override
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

    @Override
    public void removeWaiter(Id<User> userId) {
        LOGGER.warn("Removing board waiter");
        final Id<Board> toRemove = userBoardMap.remove(userId);
        if (toRemove != null) {
            boardUserMap.get(toRemove)
                    .remove(userId);
        }
    }

    @Override
    public void userDisconnected(Id<User> userId) {
        removeWaiter(userId);
        final GameSession session = gameSessionService.getSessionFor(userId);
        if (session == null) {
            LOGGER.warn("User disconnected. GameSession was null");
            return;
        }
        if (session.isMoving() || session.isReady()) {
            final FinishedMessage message = new FinishedMessage(0L, GAME_ERROR);
            session.getPlayers().forEach(player -> addFinishedMessageTask(player, message));
            gameSessionService.removeSessionForTeam(id, userId);
        } else {
            gameSessionService.removeSessionFor(userId);
        }
    }
}
