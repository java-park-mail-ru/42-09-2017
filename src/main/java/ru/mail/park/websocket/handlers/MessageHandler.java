package ru.mail.park.websocket.handlers;

import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.websocket.message.SocketMessage;

import javax.validation.constraints.NotNull;

public abstract class MessageHandler<T> {
    private Class<T> clazz;

    public MessageHandler(@NotNull Class<T> clazz) {
        this.clazz = clazz;
    }

    public void handleMessage(@NotNull SocketMessage message, @NotNull Id<User> userId) throws Exception {
        try {
            handle(clazz.cast(message), userId);
        } catch (ClassCastException e) {
            throw new Exception("Message is not convertible");
        }
    }

    public abstract void handle(@NotNull T message, @NotNull Id<User> userId) throws Exception;
}
