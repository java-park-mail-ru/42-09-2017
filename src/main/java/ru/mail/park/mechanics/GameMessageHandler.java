package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.websocket.message.from.SnapMessage;
import ru.mail.park.websocket.message.to.FinishedMessage;

import java.util.List;

import static ru.mail.park.info.constants.MessageConstants.SUCCESS;

@Service
public class GameMessageHandler {
    private final GameMechanics gameMechanics;
    private final GameSessionService gameSessionService;
    private WorldRunnerService worldRunnerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMessageHandler.class);

    GameMessageHandler(
            GameMechanics gameMechanics,
            GameSessionService gameSessionService,
            WorldRunnerService worldRunnerService
    ) {
        this.gameMechanics = gameMechanics;
        this.gameSessionService = gameSessionService;
        this.worldRunnerService = worldRunnerService;
    }

    public void handleSubscribe(Id<User> userId, Id<Board> board) {
        gameMechanics.addWaiter(userId, board);
    }

    public void handleMoving(Id<User> userId, BodyFrame snap) {
        LOGGER.info("Handle moving");
        if (!gameSessionService.isPlaying(userId) || !gameSessionService.isMovingState(userId)) {
            LOGGER.warn("I will not send snapshot because you are not playing "
                    + "or session is not in Moving state");
            return;
        }
        gameMechanics.addMovingMessageTask(userId, snap);
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
            gameMechanics.addSnapMessageTask(userId, snap);
        }
    }

    public void handleFinish(Id<User> userId) {
        LOGGER.info("Handle finish");
        gameSessionService.setFinishedForPlayer(userId);
        gameMechanics.addFinishedMessageTask(userId, new FinishedMessage(1L, SUCCESS));
        if (gameSessionService.isTeamFinished(userId)) {
            gameSessionService.setFinishedForSession(userId);
        }
    }
}
