package ru.mail.park.mechanics;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private Map<Id<User>, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

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
            } catch (IOException ignore) {
            }
        }
    }

    public void sendRowMessageTo(Id<User> userId, String message) throws IOException {
        WebSocketSession session = checkSessionFor(userId);
        try {
            session.sendMessage(new TextMessage(message));
        } catch(IOException e) {
            throw new IOException("Unable to send the message");
        }
    }

    public void sendMessageTo(@NotNull Id<User> userId, @NotNull SocketMessage message) throws IOException {
        WebSocketSession session = checkSessionFor(userId);
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch(IOException e) {
            throw new IOException("Unable to send the message");
        }
    }

    private WebSocketSession checkSessionFor(Id<User> userId) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session == null) {
            throw new IOException("No WebSocket connection");
        }
        if (!session.isOpen()) {
            throw new IOException("WebSocketConnection is closed");
        }
        return session;
    }
}
