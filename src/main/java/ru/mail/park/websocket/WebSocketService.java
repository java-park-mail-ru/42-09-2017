package ru.mail.park.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {
    private final Map<Long, WebSocketSession> sessions;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WebSocketService() {
        sessions = new ConcurrentHashMap<>();
    }

    public void registerUser(Long id, WebSocketSession webSocketSession) {
        sessions.put(id, webSocketSession);
    }


}
