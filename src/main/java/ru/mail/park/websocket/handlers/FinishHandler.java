package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.to.FinishedMessage;

import javax.annotation.PostConstruct;

@Service
public class FinishHandler extends MessageHandler<FinishedMessage> {
    private MessageHandlerContainer messageHandlerContainer;
    private GameMechanics gameMechanics;

    public FinishHandler(
            MessageHandlerContainer messageHandlerContainer,
            GameMechanics gameMechanics
    ) {
        super(FinishedMessage.class);
        this.messageHandlerContainer = messageHandlerContainer;
        this.gameMechanics = gameMechanics;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(FinishedMessage.class, this);
    }

    @Override
    public void handle(FinishedMessage message, Id<User> userId) throws Exception {
        gameMechanics.handleFinish(userId);
    }
}
