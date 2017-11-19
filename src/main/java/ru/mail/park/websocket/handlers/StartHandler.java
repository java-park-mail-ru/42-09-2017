package ru.mail.park.websocket.handlers;

import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.websocket.MessageHandlersContainer;
import ru.mail.park.websocket.message.StartMessage;

import javax.annotation.PostConstruct;

public class StartHandler extends MessageHandler<StartMessage> {
    private final MessageHandlersContainer messageHandlersContainer;
    private final GameMechanics gameMechanics;


    public StartHandler(
            MessageHandlersContainer messageHandlersContainer,
            GameMechanics gameMechanics
    ) {
        super(StartMessage.class);
        this.messageHandlersContainer = messageHandlersContainer;
        this.gameMechanics = gameMechanics;
    }

    @PostConstruct
    public void init() {
        messageHandlersContainer.registerHandler(StartMessage.class, this);
    }

    @Override
    public void handle(StartMessage message, Id<User> userId) throws Exception {
        gameMechanics.tryStartSimulation(userId, message.getSnap());
    }
}
