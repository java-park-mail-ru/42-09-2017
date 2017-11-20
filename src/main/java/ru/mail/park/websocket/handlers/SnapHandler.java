package ru.mail.park.websocket.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.WorldRunnerService;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.SnapMessage;

import javax.annotation.PostConstruct;

public class SnapHandler extends MessageHandler<SnapMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapHandler.class);
    private final WorldRunnerService worldRunnerService;
    private final MessageHandlerContainer messageHandlerContainer;

    public SnapHandler(
            WorldRunnerService worldRunnerService,
            MessageHandlerContainer messageHandlerContainer
    ) {
        super(SnapMessage.class);
        this.worldRunnerService = worldRunnerService;
        this.messageHandlerContainer = messageHandlerContainer;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(SnapMessage.class, this);
    }

    @Override
    public void handle(SnapMessage message, Id<User> userId) throws Exception {
//        worldRunnerService.
    }
}
