package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.mechanics.GameMessageHandler;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.MovingMessage;

import javax.annotation.PostConstruct;

@Service
public class MovingHandler extends MessageHandler<MovingMessage> {
    private MessageHandlerContainer messageHandlerContainer;
    private GameMessageHandler gameMessageHandler;

    public MovingHandler(
            MessageHandlerContainer messageHandlerContainer,
            GameMessageHandler gameMessageHandler
    ) {
        super(MovingMessage.class);
        this.messageHandlerContainer = messageHandlerContainer;
        this.gameMessageHandler = gameMessageHandler;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(MovingMessage.class, this);
    }

    @Override
    public void handle(MovingMessage message, Id<User> userId) throws Exception {
        gameMessageHandler.handleMoving(userId, message.getSnap());
    }
}
