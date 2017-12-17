package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.domain.Player;
import ru.mail.park.mechanics.domain.objects.BodyFrame;
import ru.mail.park.services.UserDao;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    private final Map<Id<User>, GameSession> gameSessionMap = new ConcurrentHashMap<>();
    private final Map<Id<User>, Player> playerMap = new ConcurrentHashMap<>();
    private final Map<Id<GameMechanics>, Set<GameSession>> sessionsMap = new ConcurrentHashMap<>();
    private final UserDao userDao;
    private final RemotePointService remotePointService;
    private final WorldRunnerService worldRunnerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GameSessionService.class);

    public GameSessionService(
            UserDao userDao,
            RemotePointService remotePointService,
            WorldRunnerService worldRunnerService
    ) {
        this.userDao = userDao;
        this.remotePointService = remotePointService;
        this.worldRunnerService = worldRunnerService;
    }

    public void initSessions(Set<Id<GameMechanics>> mechanicsList) {
        if (!sessionsMap.isEmpty()) {
            return;
        }
        for (Id<GameMechanics> mechanicsId : mechanicsList) {
            sessionsMap.put(mechanicsId, new HashSet<>());
        }
    }

    public Set<GameSession> getSessionsByMechanicsId(Id<GameMechanics> mechanicsId) {
        return sessionsMap.get(mechanicsId);
    }

    public boolean isMovingState(Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        return session != null && session.getState() == GameState.MOVING;
    }

    public boolean isPlaying(Id<User> userId) {
        return gameSessionMap.containsKey(userId);
    }

    public boolean isSimulationStartedFor(Id<User> userId) {
        final GameSession gameSession = gameSessionMap.get(userId);
        return gameSession == null || gameSession.getState() == GameState.SIMULATION;
    }

    public boolean isTeamReady(GameSession session) {
        return session.getPlayers().stream()
                .allMatch(id -> playerMap.get(id).isReady());
    }

    public boolean isTeamReady(Id<User> userId) {
        final GameSession session = gameSessionMap.get(userId);
        if (session == null) {
            LOGGER.error("isTeamReady() - session is null");
            return false;
        }
        return isTeamReady(session);
    }

    public boolean isTeamFinished(Id<User> userId) {
        final GameSession session = gameSessionMap.get(userId);
        if (session == null) {
            LOGGER.error("isTeamFinished() - session is null");
            return false;
        }
        return session.getPlayers().stream()
                .allMatch(id -> {
                    final Player player = playerMap.get(id);
                    return player == null || player.isFinished();
                });
    }

    public GameSession getSessionFor(Id<User> userId) {
        return gameSessionMap.get(userId);
    }

    public void prepareSimulation(Id<User> userId, List<BodyFrame> snap) {
        LOGGER.warn("Trying to start simulation");
        if (!isPlaying(userId)) {
            LOGGER.error("Should start game before simulation");
            return;
        }
        if (!isMovingState(userId)) {
            LOGGER.error("Already in simulation");
            return;
        }
        setReadyForPlayer(userId);
        getSessionFor(userId)
                .putSnapFor(userId, snap);
        if (isTeamReady(userId)) {
            setReadyForSession(userId);
        }
    }

    public void joinGame(Id<GameMechanics> mechanicsId, Id<Board> boardId, Set<Id<User>> players) {
        final GameSession gameSession = new GameSession(boardId, players);
        players.forEach(player -> {
            gameSessionMap.put(player, gameSession);
            playerMap.put(player, new Player(userDao.findUserById(player.getId())));
        });
        final Set<GameSession> sessions = sessionsMap.get(mechanicsId);
        if (sessions == null) {
            LOGGER.error("SessionsMap for this mechanics is null");
            return;
        }
        sessions.add(gameSession);
    }

    public void setMovingForSession(Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        if (session == null || session.getState() == GameState.MOVING) {
            LOGGER.warn("Session is null or already in Moving state");
            return;
        }
        session.setState(GameState.MOVING);
    }

    public void setReadyForPlayer(Id<User> userId) {
        final Player player = playerMap.get(userId);
        if (player == null) {
            LOGGER.warn("Can't set ready for player");
            return;
        }
        player.setReady(true);
    }

    public void setReadyForSession(Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        if (session == null || session.getState() == GameState.READY) {
            LOGGER.warn("Session is null or already in Ready state");
            return;
        }
        session.setState(GameState.READY);
    }

    public void setFinishedForPlayer(Id<User> userId) {
        final Player player = playerMap.get(userId);
        if (player == null) {
            LOGGER.warn("Can't set finished for player");
            return;
        }
        player.setFinished();
    }

    public void setFinishedForSession(Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        if (session == null || session.getState() == GameState.FINISHED) {
            LOGGER.warn("Session is null or already in Finished state");
            return;
        }
        session.setState(GameState.FINISHED);
    }

    public void removeSessionFor(Id<User> userId) {
        final GameSession session = gameSessionMap.remove(userId);
        if (session != null) {
            session.removePlayer(userId);
        }
        playerMap.remove(userId);
        remotePointService.cutDownConnection(userId, CloseStatus.SERVER_ERROR);
    }

    public void removeSessionForTeam(Id<GameMechanics> mechanicsId, GameSession session) {
        if (session == null) {
            return;
        }
        sessionsMap.computeIfPresent(mechanicsId, (ignore, sessionsSet) -> {
            sessionsSet.remove(session);
            return sessionsSet;
        });
        worldRunnerService.removeWorldRunnerFor(session);
    }

    public void removeSessionForTeam(Id<GameMechanics> mechanicsId, Id<User> userId) {
        final GameSession session = gameSessionMap.get(userId);
        removeSessionForTeam(mechanicsId, session);
    }

    public Player getPlayer(Id<User> userId) {
        return playerMap.get(userId);
    }

    public Set<Id<User>> getTeamOf(Id<User> userId) {
        final GameSession gameSession = gameSessionMap.get(userId);
        if (gameSession == null) {
            return null;
        }
        return gameSession.getPlayers();
    }
}
