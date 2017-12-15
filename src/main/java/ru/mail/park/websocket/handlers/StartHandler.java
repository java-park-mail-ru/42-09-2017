package ru.mail.park.websocket.handlers;

import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.GameMechanicsService;
import ru.mail.park.websocket.MessageHandlerContainer;
import ru.mail.park.websocket.message.from.StartMessage;

import javax.annotation.PostConstruct;

@Service
public class StartHandler extends MessageHandler<StartMessage> {
    private final MessageHandlerContainer messageHandlerContainer;
    private final GameMechanicsService gameMechanicsService;


    public StartHandler(
            MessageHandlerContainer messageHandlerContainer,
            GameMechanicsService gameMechanicsService
    ) {
        super(StartMessage.class);
        this.messageHandlerContainer = messageHandlerContainer;
        this.gameMechanicsService = gameMechanicsService;
    }

    @PostConstruct
    public void init() {
        messageHandlerContainer.registerHandler(StartMessage.class, this);
    }

    @Override
    public void handle(StartMessage message, Id<User> userId) throws Exception {
        gameMechanicsService.handleStart(userId, message.getBodies());
    }
}
