package ru.mail.park.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.mechanics.RemotePointService;
import ru.mail.park.mechanics.WorldParser;
import ru.mail.park.mechanics.objects.ClientSnap;
import ru.mail.park.mechanics.objects.body.BodyFrame;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.SocketMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@Service
public class SocketHandler extends TextWebSocketHandler {
    private final UserDao userDao;
    private final GameDao gameDao;
    private final RemotePointService remotePointService;
    private final MessageHandlersContainer messageHandlersContainer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    public static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in");

    public SocketHandler(
            UserDao userDao,
            GameDao gameDao,
            RemotePointService remotePointService,
            MessageHandlersContainer messageHandlersContainer
    ) {
        this.userDao = userDao;
        this.gameDao = gameDao;
        this.remotePointService = remotePointService;
        this.messageHandlersContainer = messageHandlersContainer;
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
        String textMessage = message.getPayload();
        ClientSnap snap;
        //WorldParser worldParser = gameDao.getLastParser();
        handleMessage(Id.of(userId), message);
	    return;
       // if (textMessage.equals("start")) {
       //     Thread thread = new Thread(worldParser);
       //     thread.start();
       //     LOGGER.info("Simulation started");
       //     session.sendMessage(new TextMessage("STARTED"));
       //     return;
       // } else {
       //     try {
       //         snap = mapper.readValue(message.getPayload(), ClientSnap.class);
       //         LOGGER.debug(message.getPayload());
       //     } catch (IOException e) {
       //         LOGGER.error("Wrong format");
       //         session.sendMessage(new TextMessage("WrongFormat"));
       //         return;
       //     }
       // }

       // List<BodyFrame> bodyFrames = snap.getBodies();
      //  LOGGER.info("Got changes");
      //  for (BodyFrame bodyFrame : bodyFrames) {
        //    Map<Long, BodyFrame> serverDiffs = worldParser.getDiffsPerFrame().get(bodyFrame.getId());
          //  BodyFrame serverFrame = serverDiffs.get(snap.getFrame());
            //Vec2 serverPos = new Vec2(serverFrame.getPosition().x, -serverFrame.getPosition().y);
           // Vec2 serverVel = new Vec2(serverFrame.getVelocity().x, -serverFrame.getVelocity().y);
           // float serverAngle = -serverFrame.getAngle();
           // bodyFrame.setPosition(serverPos.sub(bodyFrame.getPosition()));
           // bodyFrame.setVelocity(serverVel.sub(bodyFrame.getVelocity()));
           // bodyFrame.setAngle(serverAngle - bodyFrame.getAngle());
       // }
       // session.sendMessage(new TextMessage(mapper.writeValueAsString(snap)));
        //LOGGER.info("Message is sent");
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
            messageHandlersContainer.handle(message, userId);
        } catch (Exception e) {
            LOGGER.error("Can't handle message of type " + message.getClass().getName() + " with content: " + textMessage, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
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
