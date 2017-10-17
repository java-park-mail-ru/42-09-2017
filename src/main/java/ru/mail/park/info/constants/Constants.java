package ru.mail.park.info.constants;

public class Constants {
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String EMAIL_REGEXP =
            "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
    public static final String USERNAME_REGEXP =
            "[A-Za-z][A-Za-z0-9]*?([-_][A-Za-z0-9]+){0,2}";
}
