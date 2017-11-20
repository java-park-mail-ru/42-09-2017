package ru.mail.park.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.websocket.handlers.MessageHandler;
import ru.mail.park.websocket.message.SocketMessage;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageHandlerContainer {
    private Map<Class<?>, MessageHandler<?>> handlersMap = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(MessageHandlerContainer.class);

    public void handle(@NotNull SocketMessage message, @NotNull Id<User> userId) throws Exception {
        final MessageHandler<?> messageHandler = handlersMap.get(message.getClass());
        for (Map.Entry<Class<?>, MessageHandler<?>> entry : handlersMap.entrySet()) {
            logger.warn("message class: " + entry.getKey().getName());
            logger.warn("handler class: " + entry.getValue().getClass().getName());
        }
        if (messageHandler == null) {
            throw new Exception("Unknown message type");
        }
        messageHandler.handleMessage(message, userId);
    }

    public <T extends SocketMessage> void registerHandler(
            @NotNull Class<T> clazz,
            @NotNull MessageHandler<T> messageHandler
    ) {
        handlersMap.put(clazz, messageHandler);
    }
}
