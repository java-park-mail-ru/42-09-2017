package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.FinishMessage;

import javax.annotation.PostConstruct;

@Service
public class FinishHandler extends MessageHandler<FinishMessage> {
    private MessageHandlerContainer messageHandlerContainer;
    private GameMechanics gameMechanics;

    public FinishHandler(
            MessageHandlerContainer messageHandlerContainer,
            GameMechanics gameMechanics
    ) {
        super(FinishMessage.class);
        this.messageHandlerContainer = messageHandlerContainer;
        this.gameMechanics = gameMechanics;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(FinishMessage.class, this);
    }

    @Override
    public void handle(FinishMessage message, Id<User> userId) throws Exception {
        gameMechanics.handleFinish(userId);
    }
}
