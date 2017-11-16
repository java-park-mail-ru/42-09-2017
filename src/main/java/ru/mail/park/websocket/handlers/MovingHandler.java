package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.websocket.MessageHandlersContainer;
import ru.mail.park.websocket.message.MovingMessage;

import javax.annotation.PostConstruct;

@Service
public class MovingHandler extends MessageHandler<MovingMessage> {
    private MessageHandlersContainer messageHandlersContainer;

    public MovingHandler(
            MessageHandlersContainer messageHandlersContainer
    ) {
        super(MovingMessage.class);
        this.messageHandlersContainer = messageHandlersContainer;
    }

    @PostConstruct
    public void init() {
        messageHandlersContainer.registerHandler(MovingMessage.class, this);
    }

    @Override
    public void handle(MovingMessage message, Id<User> userId) throws Exception {

    }
}
