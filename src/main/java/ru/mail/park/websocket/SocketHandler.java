package ru.mail.park.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanicsService;
import ru.mail.park.mechanics.RemotePointService;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.SocketMessage;

import java.io.IOException;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;
import static ru.mail.park.info.constants.Constants.OAUTH_VK_ATTR;
import static ru.mail.park.info.constants.Constants.SESSION_ATTR;

public class SocketHandler extends TextWebSocketHandler {
    private final UserDao userDao;
    private final GameMechanicsService gameMechanicsService;
    private final RemotePointService remotePointService;
    private final MessageHandlerContainer messageHandlerContainer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    private static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in");

    public SocketHandler(
            UserDao userDao,
            GameMechanicsService gameMechanicsService,
            RemotePointService remotePointService,
            MessageHandlerContainer messageHandlerContainer
    ) {
        this.userDao = userDao;
        this.gameMechanicsService = gameMechanicsService;
        this.remotePointService = remotePointService;
        this.messageHandlerContainer = messageHandlerContainer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        final Long userId = (Long) session.getAttributes().get(SESSION_ATTR);
        final String vkToken = (String) session.getAttributes().get(OAUTH_VK_ATTR);

        if (userId != null && userDao.findUserById(userId) != null) {
            remotePointService.registerUser(Id.of(userId), session);
            LOGGER.info("CONNECTED");
            return;
        } else if (vkToken != null) {
            final User user = userDao.findUserVkByToken(vkToken);
            if (user != null) {
                remotePointService.registerUser(Id.of(user.getId()), session);
                LOGGER.info("CONNECTED");
            }
            return;
        }
        LOGGER.warn("Empty HTTP session. Closing");
        closeSession(session, ACCESS_DENIED);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!session.isOpen()) {
            LOGGER.warn("Warning. Session is not opened");
            return;
        }
        final Long userId = (Long) session.getAttributes().get(SESSION_ATTR);
        final String vkToken = (String) session.getAttributes().get(OAUTH_VK_ATTR);

        if (userId != null && userDao.findUserById(userId) != null) {
            handleMessage(Id.of(userId), message);
            return;
        } else if (vkToken != null) {
            final User user = userDao.findUserVkByToken(vkToken);
            if (user != null) {
                handleMessage(Id.of(user.getId()), message);
                return;
            }
        }
        LOGGER.warn("Empty HTTP session. Closing");
        closeSession(session, ACCESS_DENIED);
    }

    private void handleMessage(Id<User> userId, TextMessage textMessage) {
        final SocketMessage message;
        try {
            message = mapper.readValue(textMessage.getPayload(), SocketMessage.class);
        } catch (IOException ex) {
            LOGGER.error("wrong json format at game response", ex);
            return;
        }
        try {
            messageHandlerContainer.handle(message, userId);
        } catch (Exception e) {
            LOGGER.error("Can't handle message of type " + message.getClass().getName() + " with content: " + textMessage, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final Long id = (Long) session.getAttributes().get(SESSION_ATTR);
        final String vkToken = (String) session.getAttributes().get(OAUTH_VK_ATTR);

        if (id != null) {
            gameMechanicsService.handleDisconnect(Id.of(id));
        } else if (vkToken != null) {
            final User user = userDao.findUserVkByToken(vkToken);
            if (user != null) {
                gameMechanicsService.handleDisconnect(Id.of(user.getId()));
            }
        }
        LOGGER.warn("CLOSED");
    }

    private void closeSession(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        final CloseStatus status;
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
