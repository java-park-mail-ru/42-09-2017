package ru.mail.park.controllers.messages;

import com.fasterxml.jackson.annotation.JsonView;
import ru.mail.park.view.View;
import ru.mail.park.models.User;

public class UserMessage<T> extends Message<T> {
    private User user;

    public UserMessage(T message, User user) {
        super(message);
        this.user = user;
    }

    @Override
    @JsonView(View.SummaryWithMessage.class)
    public T getMessage() {
        return super.getMessage();
    }

    @JsonView(View.Summary.class)
    public User getUser() {
        return user;
    }
}
