package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.mechanics.GameMessageHandler;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.SubscribeMessage;

import javax.annotation.PostConstruct;

@Service
public class SubscribeHandler extends MessageHandler<SubscribeMessage> {
    private GameMessageHandler gameMessageHandler;
    private MessageHandlerContainer handlersContainer;

    public SubscribeHandler(
            GameMessageHandler gameMessageHandler,
            MessageHandlerContainer handlersContainer
    ) {
        super(SubscribeMessage.class);
        this.gameMessageHandler = gameMessageHandler;
        this.handlersContainer = handlersContainer;
    }

    @PostConstruct
    public void init() {
        handlersContainer.registerHandler(SubscribeMessage.class, this);
    }

    @Override
    public void handle(SubscribeMessage message, Id<User> userId) throws Exception {
        gameMessageHandler.handleSubscribe(userId, message.getBoard());
    }
}
