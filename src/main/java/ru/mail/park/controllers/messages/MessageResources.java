package ru.mail.park.controllers.messages;

public enum MessageResources {
    SIGNED_UP("Successfully signed up"),
    UPDATED("Updated successfully"),
    LOGGED_IN("Logged_in"),
    LOGGED_OUT("Logged_out"),

    UNAUTHORIZED("You are not authorized"),
    BAD_LOGIN_DATA("Invalid username or password"),
    BAD_USERNAME("Invalid username"),
    BAD_EMAIL("Invalid email address"),
    BAD_OLD_PASSWORD("Invalid old password"),
    BAD_PASSWORD("Invalid password"),

    EMPTY_USERNAME("Username field is empty"),
    EMPTY_EMAIL("Email field is empty"),
    EMPTY_OLD_PASSWORD("Old password is necessary for changing password"),
    EMPTY_PASSWORD("Password field is empty"),

    SHORT_USERNAME("Username is too short"),

    EXISTS_USERNAME("Username already exists"),
    EXISTS_EMAIL("Email address already exists")
    ;



    private Message message;

    MessageResources(String message) {
        this.message = new Message(message);
    }

    public Message getMessage() {
        return message;
    }
}
