package ru.mail.park.controllers.messages;

public enum MessageResources {
    SIGNED_UP("SIGNED_UP"),
    UPDATED("UPDATED"),
    LOGGED_IN("LOGGED_IN"),
    LOGGED_OUT("LOGGED_OUT"),

    UNAUTHORIZED("NOT_AUTHORIZED"),
    BAD_LOGIN_DATA("LOGIN_DATA_BAD"),
    BAD_USERNAME("USERNAME_FIELD_BAD"),
    BAD_EMAIL("EMAIL_FIELD_BAD"),
    BAD_OLD_PASSWORD("OLD_PASSWORD_BAD"),
    BAD_PASSWORD("PASSWORD_FIELD_BAD"),

    EMPTY_USERNAME("USERNAME_FIELD_EMPTY"),
    EMPTY_EMAIL("EMAIL_FIELD_EMPTY"),
    EMPTY_OLD_PASSWORD("OLD_PASSWORD_EMPTY"),
    EMPTY_PASSWORD("PASSWORD_FIELD_EMPTY"),

    SHORT_USERNAME("USERNAME_FIELD_TOO_SHORT"),

    EXISTS_USERNAME("USERNAME_ALREADY_EXISTS"),
    EXISTS_EMAIL("EMAIL_ALREADY_EXISTS");



    private Message message;

    MessageResources(String message) {
        this.message = new Message(message);
    }

    public Message getMessage() {
        return message;
    }
}
