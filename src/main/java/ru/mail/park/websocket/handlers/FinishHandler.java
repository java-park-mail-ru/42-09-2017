package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMessageHandler;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.FinishMessage;

import javax.annotation.PostConstruct;

@Service
public class FinishHandler extends MessageHandler<FinishMessage> {
    private MessageHandlerContainer messageHandlerContainer;
    private GameMessageHandler gameMessageHandler;

    public FinishHandler(
            MessageHandlerContainer messageHandlerContainer,
            GameMessageHandler gameMessageHandler
    ) {
        super(FinishMessage.class);
        this.messageHandlerContainer = messageHandlerContainer;
        this.gameMessageHandler = gameMessageHandler;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(FinishMessage.class, this);
    }

    @Override
    public void handle(FinishMessage message, Id<User> userId) throws Exception {
        gameMessageHandler.handleFinish(userId);
    }
}
