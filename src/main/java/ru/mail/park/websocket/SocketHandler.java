package ru.mail.park.websocket;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.services.UserDao;

import java.io.IOException;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@Service
public class SocketHandler extends TextWebSocketHandler {
    private final UserDao userDao;
    private final WebSocketService webSocketService;

    public static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in");

    public SocketHandler(UserDao userDao, WebSocketService webSocketService) {
        this.userDao = userDao;
        this.webSocketService = webSocketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long id = (Long) session.getAttributes().get(Constants.SESSION_ATTR);
        if (id == null || userDao.findUserById(id) == null) {
            closeSession(session, ACCESS_DENIED);
            return;
        }
        webSocketService.registerUser(id, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long id = (Long) session.getAttributes().get(Constants.SESSION_ATTR);

        if (id == null || userDao.findUserById(id) == null) {
            closeSession(session, ACCESS_DENIED);
            return;
        }
        String response = "WebSocket message to Danya";
        session.sendMessage(new TextMessage(response));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
