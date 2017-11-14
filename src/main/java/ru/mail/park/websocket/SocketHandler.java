package ru.mail.park.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.mechanics.WorldParser;
import ru.mail.park.mechanics.objects.ClientSnap;
import ru.mail.park.mechanics.objects.body.BodyDiff;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@Service
public class SocketHandler extends TextWebSocketHandler {
    private final UserDao userDao;
    private final GameDao gameDao;
    private final WebSocketService webSocketService;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    public static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in");

    public SocketHandler(UserDao userDao, GameDao gameDao, WebSocketService webSocketService) {
        this.userDao = userDao;
        this.gameDao = gameDao;
        this.webSocketService = webSocketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        Long id = (Long) session.getAttributes().get(Constants.SESSION_ATTR);
//        if (id == null || userDao.findUserById(id) == null) {
//            LOGGER.warn("Access denied");
//            closeSession(session, ACCESS_DENIED);
//            return;
//        }
//        webSocketService.registerUser(id, session);
        LOGGER.info("CONNECTED");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        Long id = (Long) session.getAttributes().get(Constants.SESSION_ATTR);
//
//        if (id == null || userDao.findUserById(id) == null) {
//            LOGGER.warn("Message is not handled");
//            closeSession(session, ACCESS_DENIED);
//            return;
//        }
        LOGGER.info("Message received");
        String textMessage = message.getPayload();
        ClientSnap snap;
        WorldParser worldParser = gameDao.getLastParser();
        if (textMessage.equals("start")) {
            Thread thread = new Thread(worldParser);
            thread.start();
            LOGGER.info("Simulation started");
            session.sendMessage(new TextMessage("STARTED"));
            return;
        } else {
            try {
                snap = mapper.readValue(message.getPayload(), ClientSnap.class);
                LOGGER.debug(message.getPayload());
            } catch (IOException e) {
                LOGGER.error("Wrong format");
                session.sendMessage(new TextMessage("WrongFormat"));
                return;
            }
        }

        List<BodyDiff> bodyDiffs = snap.getBodies();
        LOGGER.info("Got changes");

        for (BodyDiff bodyDiff : bodyDiffs) {
            LOGGER.warn("   body #" + bodyDiff.getId().toString());
            Map<Long, BodyDiff> serverDiffs = worldParser.getDiffsPerFrame().get(bodyDiff.getId());
            LOGGER.warn("   frame #" + snap.getFrame().toString());
            LOGGER.warn("   fnumber " + String.valueOf(serverDiffs.size()));
            BodyDiff serverDiff = serverDiffs.get(snap.getFrame());
            Vec2 serverPos = new Vec2(serverDiff.getPosition().x, - serverDiff.getPosition().y);
            float serverAngle = - serverDiff.getAngle();
            bodyDiff.setPosition(serverPos.sub(bodyDiff.getPosition()));
            bodyDiff.setAngle(serverAngle - bodyDiff.getAngle());
        }

        session.sendMessage(new TextMessage(mapper.writeValueAsString(snap)));
        LOGGER.info("Message is sent");
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
