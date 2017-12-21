package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.mechanics.domain.Player;
import ru.mail.park.mechanics.domain.objects.BodyFrame;
import ru.mail.park.services.UserDao;

import javax.validation.constraints.NotNull;
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

    public void initSessions(@NotNull Set<Id<GameMechanics>> mechanicsList) {
        if (!sessionsMap.isEmpty()) {
            return;
        }
        for (Id<GameMechanics> mechanicsId : mechanicsList) {
            sessionsMap.put(mechanicsId, new HashSet<>());
        }
    }

    public Set<GameSession> getSessionsByMechanicsId(@NotNull Id<GameMechanics> mechanicsId) {
        return sessionsMap.get(mechanicsId);
    }

    public boolean isMovingState(@NotNull Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        return session != null && session.getState() == GameState.MOVING;
    }

    public boolean isPlaying(@NotNull Id<User> userId) {
        return gameSessionMap.containsKey(userId);
    }

    public boolean isSimulationStartedFor(@NotNull Id<User> userId) {
        final GameSession gameSession = gameSessionMap.get(userId);
        return gameSession == null || gameSession.getState() == GameState.SIMULATION;
    }

    public boolean isTeamReady(@NotNull GameSession session) {
        return session.getPlayers().stream()
                .allMatch(id -> playerMap.get(id).isReady());
    }

    public boolean isTeamReady(@NotNull Id<User> userId) {
        final GameSession session = gameSessionMap.get(userId);
        if (session == null) {
            LOGGER.error("isTeamReady() - session is null");
            return false;
        }
        return isTeamReady(session);
    }

    public boolean isTeamFinished(@NotNull Id<User> userId) {
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

    public GameSession getSessionFor(@NotNull Id<User> userId) {
        return gameSessionMap.get(userId);
    }

    public void prepareSimulation(@NotNull Id<User> userId, @NotNull List<BodyFrame> snap) {
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

    public void joinGame(@NotNull Id<GameMechanics> mechanicsId,
                         @NotNull Id<Board> boardId,
                         @NotNull BoardRequest.Data board,
                         @NotNull Set<Id<User>> players
    ) {
        final GameSession gameSession = new GameSession(boardId, board, players);
        players.forEach(player -> {
            gameSessionMap.put(player, gameSession);
            playerMap.put(player, new Player(userDao, userDao.findUserById(player.getId())));
        });
        final Set<GameSession> sessions = sessionsMap.get(mechanicsId);
        if (sessions == null) {
            LOGGER.error("SessionsMap for this mechanics is null");
            return;
        }
        sessions.add(gameSession);
    }

    public void setMovingForSession(@NotNull Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        if (session == null || session.getState() == GameState.MOVING) {
            LOGGER.warn("Session is null or already in Moving state");
            return;
        }
        session.setState(GameState.MOVING);
    }

    public void setReadyForPlayer(@NotNull Id<User> userId) {
        final Player player = playerMap.get(userId);
        if (player == null) {
            LOGGER.warn("Can't set ready for player");
            return;
        }
        player.setReady(true);
    }

    public void setReadyForSession(@NotNull Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        if (session == null || session.getState() == GameState.READY) {
            LOGGER.warn("Session is null or already in Ready state");
            return;
        }
        session.setState(GameState.READY);
    }

    public void setFinishedForPlayer(@NotNull Id<User> userId) {
        final Player player = playerMap.get(userId);
        if (player == null) {
            LOGGER.warn("Can't set finished for player");
            return;
        }
        player.setFinished();
    }

    public void setFinishedForSession(@NotNull Id<User> userId) {
        final GameSession session = getSessionFor(userId);
        if (session == null || session.getState() == GameState.FINISHED) {
            LOGGER.warn("Session is null or already in Finished state");
            return;
        }
        session.setState(GameState.FINISHED);
    }

    public void removeSessionFor(@NotNull Id<User> userId) {
        final GameSession session = gameSessionMap.remove(userId);
        if (session != null) {
            session.removePlayer(userId);
        }
        playerMap.remove(userId);
        remotePointService.cutDownConnection(userId, CloseStatus.SERVER_ERROR);
    }

    public void removeSessionForTeam(@NotNull Id<GameMechanics> mechanicsId, @NotNull GameSession session) {
        if (session == null) {
            return;
        }
        sessionsMap.computeIfPresent(mechanicsId, (ignore, sessionsSet) -> {
            sessionsSet.remove(session);
            return sessionsSet;
        });
        worldRunnerService.removeWorldRunnerFor(session);
    }

    public void removeSessionForTeam(@NotNull Id<GameMechanics> mechanicsId, @NotNull Id<User> userId) {
        final GameSession session = gameSessionMap.get(userId);
        removeSessionForTeam(mechanicsId, session);
    }

    public Player getPlayer(Id<User> userId) {
        return playerMap.get(userId);
    }

    public void setPlayerId(@NotNull Id<User> userId, @NotNull Id<Player> playerId) {
        final Player player = getPlayer(userId);
        if (player != null) {
            player.setId(playerId);
        }
    }

    public Set<Id<User>> getTeamOf(@NotNull Id<User> userId) {
        final GameSession gameSession = gameSessionMap.get(userId);
        if (gameSession == null) {
            return null;
        }
        return gameSession.getPlayers();
    }

    public void setScores(@NotNull GameSession session) {
        final WorldRunner worldRunner = worldRunnerService.getWorldRunnerFor(session);
        if (worldRunner == null) {
            return;
        }
        final Map<Id<Player>, Long> scoreMap = worldRunner.getPlayerScoreMap();
        session.getPlayers()
                .forEach(userId -> {
                    final Player player = getPlayer(userId);
                    final Long score = scoreMap.getOrDefault(player.getId(), 0L);
                    player.setScore(score);
                });
    }
}
