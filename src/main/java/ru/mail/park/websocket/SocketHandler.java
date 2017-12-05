package ru.mail.park.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.mechanics.GameSessionService;
import ru.mail.park.mechanics.RemotePointService;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.SocketMessage;

import java.io.IOException;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

public class SocketHandler extends TextWebSocketHandler {
    private final UserDao userDao;
    private final GameDao gameDao;
    private final GameMechanics gameMechanics;
    private final RemotePointService remotePointService;
    private final GameSessionService gameSessionService;
    private final MessageHandlerContainer messageHandlerContainer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    public static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in");

    public SocketHandler(
            UserDao userDao,
            GameDao gameDao,
            GameMechanics gameMechanics,
            RemotePointService remotePointService,
            GameSessionService gameSessionService,
            MessageHandlerContainer messageHandlerContainer
    ) {
        this.userDao = userDao;
        this.gameDao = gameDao;
        this.gameMechanics = gameMechanics;
        this.remotePointService = remotePointService;
        this.gameSessionService = gameSessionService;
        this.messageHandlerContainer = messageHandlerContainer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get(Constants.SESSION_ATTR);
        if (userId == null || userDao.findUserById(userId) == null) {
            LOGGER.warn("Empty HTTP session. Closing");
            closeSession(session, ACCESS_DENIED);
            return;
        }
        remotePointService.registerUser(Id.of(userId), session);
        LOGGER.info("CONNECTED");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!session.isOpen()) {
            LOGGER.warn("Warning. Session is not opened");
            return;
        }
        Long userId = (Long) session.getAttributes().get(Constants.SESSION_ATTR);
        if (userId == null || userDao.findUserById(userId) == null) {
            LOGGER.warn("Empty HTTP session. Closing");
            closeSession(session, ACCESS_DENIED);
            return;
        }
        handleMessage(Id.of(userId), message);
    }

    public void handleMessage(Id<User> userId, TextMessage textMessage) {
        final SocketMessage message;
        try {
            message = mapper.readValue(textMessage.getPayload(), SocketMessage.class);
        } catch (IOException ex) {
            LOGGER.error("wrong json format at game response", ex);
            return;
        }
        try {
            //noinspection ConstantConditions
            messageHandlerContainer.handle(message, userId);
        } catch (Exception e) {
            LOGGER.error("Can't handle message of type " + message.getClass().getName() + " with content: " + textMessage, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long id = (Long) session.getAttributes().get(Constants.SESSION_ATTR);
        if (id == null) {
            return;
        }
        gameMechanics.userDisconnected(Id.of(id));
        LOGGER.warn("CLOSED");
    }

    private void closeSession(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        CloseStatus status;
        if (closeStatus == null) {
            status = SERVER_ERROR;
        } else {
            status = closeStatus;
        }
        try {
            webSocketSession.close(status);
            LOGGER.error("Connection is closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
