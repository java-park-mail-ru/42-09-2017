package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.websocket.MessageHandlersContainer;
import ru.mail.park.websocket.message.SubscribeMessage;

import javax.annotation.PostConstruct;

@Service
public class SubscribeHandler extends MessageHandler<SubscribeMessage> {
    private GameMechanics gameMechanics;
    private MessageHandlersContainer handlersContainer;

    public SubscribeHandler(
            GameMechanics gameMechanics,
            MessageHandlersContainer handlersContainer
    ) {
        this.gameMechanics = gameMechanics;
        this.handlersContainer = handlersContainer;
    }

    @PostConstruct
    public void init() {
        handlersContainer.registerHandler(SubscribeMessage.class, this);
    }

    @Override
    public void handle(SubscribeMessage message, Id<User> userId) throws Exception {
        gameMechanics.tryStartGame(userId, message.getBoard());
    }
}
