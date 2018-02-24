package ru.mail.park.websocket.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.exceptions.FramesOutOfBoundException;
import ru.mail.park.mechanics.GameMechanicsService;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.SnapMessage;

import javax.annotation.PostConstruct;

@Service
public class SnapHandler extends MessageHandler<SnapMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapHandler.class);
    private final GameMechanicsService gameMechanicsService;
    private final MessageHandlerContainer messageHandlerContainer;

    public SnapHandler(
            GameMechanicsService gameMechanicsService,
            MessageHandlerContainer messageHandlerContainer
    ) {
        super(SnapMessage.class);
        this.gameMechanicsService = gameMechanicsService;
        this.messageHandlerContainer = messageHandlerContainer;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(SnapMessage.class, this);
    }

    @Override
    public void handle(SnapMessage message, Id<User> userId) {
        try {
            gameMechanicsService.handleSnap(userId, message);
        } catch (NullPointerException e) {
            LOGGER.warn("There could appear NullPointerException. But doesn't. LOL");
        } catch (FramesOutOfBoundException e) {
            gameMechanicsService.handleFinish(userId);
        }
    }
}
