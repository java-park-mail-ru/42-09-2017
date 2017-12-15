package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.websocket.message.from.SnapMessage;
import ru.mail.park.websocket.message.to.FinishedMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.mail.park.info.constants.Constants.THREAD_POOL_SIZE;

@Service
public class GameMechanicsService {
    private final ApplicationContext applicationContext;
    private Map<Id<GameMechanics>, GameMechanics> idMechanicsMap = new ConcurrentHashMap<>();
    private Map<Id<User>, GameMechanics> gameMechanicsMap = new ConcurrentHashMap<>();
    private final GameSessionService gameSessionService;
    private WorldRunnerService worldRunnerService;

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

    public List<GameMechanics> initMechanics(int count) {
        if (!idMechanicsMap.isEmpty()) {
            return new ArrayList<>(idMechanicsMap.values());
        }
        for (int i = 0; i < count; i++) {
            GameMechanicsImpl mechanics = applicationContext.getBean(GameMechanicsImpl.class);
            mechanics.setId(i);
            idMechanicsMap.put(mechanics.getId(), mechanics);
        }
        return new ArrayList<>(idMechanicsMap.values());
    }

    private Id<GameMechanics> calculateMechanicsId(Id<Board> boardId) {
        return Id.of(boardId.getId() % THREAD_POOL_SIZE);
    }

    public void handleSubscribe(Id<User> userId, Id<Board> boardId) {
        GameMechanicsImpl mechanics = (GameMechanicsImpl) gameMechanicsMap.get(userId);
        Id<GameMechanics> newMechanicsId = calculateMechanicsId(boardId);
        GameMechanics mechanicsNew = idMechanicsMap.get(newMechanicsId);
        if (mechanics != null) {
            if (mechanics.getId().equals(newMechanicsId)) {
                mechanics.addWaiter(userId, boardId);
                return;
            } else if (!gameSessionService.isPlaying(userId)) {
                mechanics.removeWaiter(userId);
                gameMechanicsMap.remove(userId);
            } else {
                LOGGER.error("Can't subscribe. User is playing.");
                return;
            }
        }
        LOGGER.warn("Putting new player into mechanics #" + newMechanicsId);
        boolean added = mechanicsNew.addWaiter(userId, boardId);
        if (added) {
            gameMechanicsMap.put(userId, mechanicsNew);
        }
    }

    public void handleMoving(Id<User> userId, BodyFrame snap) {
        LOGGER.info("Handle moving");
        if (!gameSessionService.isPlaying(userId) || !gameSessionService.isMovingState(userId)) {
            LOGGER.warn("I will not send snapshot because you are not playing "
                    + "or session is not in Moving state");
            return;
        }
        gameMechanicsMap.get(userId).addMovingMessageTask(userId, snap);
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
        boolean cheat = worldRunnerService.checkSnap(session, snap);
        if (cheat) {
            gameMechanicsMap.get(userId).addSnapMessageTask(userId, snap);
        }
    }

    public void handleFinish(Id<User> userId) {
        LOGGER.info("Handle finish");
        Player player = gameSessionService.getPlayer(userId);
        if (player.isFinished()) {
            return;
        }
        gameSessionService.setFinishedForPlayer(userId);
        GameSession session = gameSessionService.getSessionFor(userId);
        FinishedMessage message = new FinishedMessage(player.getScore(), session.getResult());
        gameMechanicsMap.get(userId).addFinishedMessageTask(userId, message);
        if (gameSessionService.isTeamFinished(userId)) {
            gameSessionService.setFinishedForSession(userId);
        }
    }

    public void handleDisconnect(Id<User> userId) {
        GameMechanics mechanics = gameMechanicsMap.get(userId);
        if (mechanics != null) {
            mechanics.userDisconnected(userId);
        }
    }
}
