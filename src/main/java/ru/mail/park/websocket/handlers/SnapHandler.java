package ru.mail.park.websocket.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanics;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.SnapMessage;

import javax.annotation.PostConstruct;

@Service
public class SnapHandler extends MessageHandler<SnapMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapHandler.class);
    private final GameMechanics gameMechanics;
    private final MessageHandlerContainer messageHandlerContainer;

    public SnapHandler(
            GameMechanics gameMechanics,
            MessageHandlerContainer messageHandlerContainer
    ) {
        super(SnapMessage.class);
        this.gameMechanics = gameMechanics;
        this.messageHandlerContainer = messageHandlerContainer;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(SnapMessage.class, this);
    }

    @Override
    public void handle(SnapMessage message, Id<User> userId) throws Exception {
        try {
            gameMechanics.handleSnap(userId, message);
        } catch (NullPointerException e) {
            LOGGER.warn("There could appear NullPointerException. But doesn't. LOL");
        }
    }
}
