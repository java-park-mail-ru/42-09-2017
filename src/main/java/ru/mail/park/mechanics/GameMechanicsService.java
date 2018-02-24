package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.domain.Player;
import ru.mail.park.mechanics.domain.objects.BodyFrame;
import ru.mail.park.websocket.message.from.SnapMessage;
import ru.mail.park.websocket.message.to.FinishedMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static ru.mail.park.info.constants.Constants.THREAD_POOL_SIZE;

@Service
public class GameMechanicsService {
    private final ApplicationContext applicationContext;
    private final Map<Id<GameMechanics>, GameMechanics> idMechanicsMap = new ConcurrentHashMap<>();
    private final Map<Id<User>, GameMechanics> gameMechanicsMap = new ConcurrentHashMap<>();
    private final GameSessionService gameSessionService;
    private final WorldRunnerService worldRunnerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMechanicsService.class);

    GameMechanicsService(
            ApplicationContext applicationContext,
            GameSessionService gameSessionService,
            WorldRunnerService worldRunnerService
    ) {
        this.applicationContext = applicationContext;
        this.gameSessionService = gameSessionService;
        this.worldRunnerService = worldRunnerService;
    }

    public Set<GameMechanics> initMechanics(int count) {
        if (!idMechanicsMap.isEmpty()) {
            return new HashSet<>(idMechanicsMap.values());
        }
        final Set<GameMechanics> resultSet = new HashSet<>();
        for (int i = 0; i < count; i++) {
            final GameMechanicsImpl mechanics = applicationContext.getBean(GameMechanicsImpl.class);
            mechanics.setId(i);
            resultSet.add(mechanics);
            idMechanicsMap.put(mechanics.getId(), mechanics);
        }
        gameSessionService.initSessions(idMechanicsMap.keySet());
        return resultSet;
    }

    private Id<GameMechanics> calculateMechanicsId(Id<Board> boardId) {
        return Id.of(boardId.getId() % THREAD_POOL_SIZE);
    }

    public boolean handleSubscribe(Id<User> userId, Id<Board> boardId) {
        LOGGER.info("Handle subscribe");
        final GameMechanicsImpl mechanics = (GameMechanicsImpl) gameMechanicsMap.get(userId);
        final Id<GameMechanics> newMechanicsId = calculateMechanicsId(boardId);
        final GameMechanics mechanicsNew = idMechanicsMap.get(newMechanicsId);
        boolean added;
        if (mechanics != null) {
            if (mechanics.getId().equals(newMechanicsId)) {
                added = mechanics.addWaiter(userId, boardId);
                return added;
            } else if (!gameSessionService.isPlaying(userId)) {
                mechanics.removeWaiter(userId);
                gameMechanicsMap.remove(userId);
            } else {
                LOGGER.error("Can't subscribe. User is playing.");
                return false;
            }
        }
        LOGGER.warn("Putting new player into mechanics #" + newMechanicsId);
        added = mechanicsNew.addWaiter(userId, boardId);
        if (added) {
            gameMechanicsMap.put(userId, mechanicsNew);
        }
        return added;
    }

    public boolean handleMoving(Id<User> userId, BodyFrame snap) {
        LOGGER.info("Handle moving");
        if (!gameSessionService.isPlaying(userId) || !gameSessionService.isMovingState(userId)) {
            LOGGER.warn("I will not send snapshot because you are not playing "
                    + "or session is not in Moving state");
            return false;
        }
        gameMechanicsMap.get(userId).addMovingMessageTask(userId, snap);
        return true;
    }

    public void handleStart(Id<User> userId, List<BodyFrame> snap) {
        LOGGER.info("Handle start");
        gameSessionService.prepareSimulation(userId, snap);
    }

    public boolean handleSnap(Id<User> userId, SnapMessage snap) throws NullPointerException {
        LOGGER.info("Handle snap");
        final GameSession session = gameSessionService.getSessionFor(userId);
        if (session == null) {
            LOGGER.error("Can't handle snap. Session is null");
            return false;
        }
        final boolean cheat = worldRunnerService.checkSnap(session, snap);
        if (cheat) {
            gameMechanicsMap.get(userId).addSnapMessageTask(userId, snap);
        }
        return true;
    }

    public boolean handleFinish(Id<User> userId) {
        LOGGER.info("Handle finish");
        final Player player = gameSessionService.getPlayer(userId);
        if (!gameSessionService.isPlaying(userId) || player.isFinished()) {
            return false;
        }
        gameSessionService.setFinishedForPlayer(userId);
        final GameSession session = gameSessionService.getSessionFor(userId);
        final FinishedMessage message = new FinishedMessage(player.getScore(), session.getResult());
        gameMechanicsMap.get(userId).addFinishedMessageTask(userId, message);
        if (gameSessionService.isTeamFinished(userId)) {
            gameSessionService.setFinishedForSession(userId);
        }
        return true;
    }

    public void handleDisconnect(Id<User> userId) {
        final GameMechanics mechanics = gameMechanicsMap.get(userId);
        if (mechanics != null) {
            mechanics.userDisconnected(userId);
        }
    }
}
