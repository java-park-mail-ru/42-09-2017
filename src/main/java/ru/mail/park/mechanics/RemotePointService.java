package ru.mail.park.mechanics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.websocket.message.SocketMessage;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RemotePointService {
    private final Map<Id<User>, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotePointService.class);

    public void registerUser(Id<User> userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public boolean isConnected(Id<User> userId) {
        return sessions.containsKey(userId) && sessions.get(userId).isOpen();
    }

    public void cutDownConnection(@NotNull Id<User> userId, @NotNull CloseStatus closeStatus) {
        final WebSocketSession webSocketSession = sessions.get(userId);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close(closeStatus);
            } catch (IOException e) {
                LOGGER.warn("Cut down exception");
            }
        }
    }

    public void sendMessageTo(@NotNull Id<User> userId, @NotNull SocketMessage message) throws IOException {
        final WebSocketSession session = checkSessionFor(userId);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                LOGGER.info("SENT MESSAGE BY SOCKET");
            } catch (IOException e) {
                throw new IOException("Unable to send the message", e);
            }
        }
    }

    private WebSocketSession checkSessionFor(Id<User> userId) throws IOException {
        final WebSocketSession session = sessions.get(userId);
        if (session == null) {
            throw new IOException("No WebSocket connection");
        }
        if (!session.isOpen()) {
            throw new IOException("WebSocketConnection is closed");
        }
        return session;
    }
}
