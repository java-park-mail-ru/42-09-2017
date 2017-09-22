package ru.mail.park.controllers.messages;

import com.fasterxml.jackson.annotation.JsonView;
import ru.mail.park.view.View;
import ru.mail.park.models.User;

public class UserMessage {
    private User user;

    public UserMessage(User user) {
        this.user = user;
    }

    @JsonView(View.Summary.class)
    public User getUser() {
        return user;
    }
}
