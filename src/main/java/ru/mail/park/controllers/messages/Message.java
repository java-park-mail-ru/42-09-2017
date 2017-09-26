package ru.mail.park.controllers.messages;

public class Message<T> {
    private T message;

    public Message(T message) {
        this.message = message;
    }

    public T getMessage() {
        return message;
    }
}
