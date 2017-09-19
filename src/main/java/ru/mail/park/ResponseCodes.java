package ru.mail.park;

public class ResponseCodes {
    public static final Integer SUCCESS = 0;

    public static final Integer EMAIL_FIELD_EMPTY = 1000;
    public static final Integer EMAIL_FIELD_BAD = 1001;
    public static final Integer EMAIL_ALREADY_EXISTS = 1002;

    public static final Integer USERNAME_FIELD_EMPTY = 1100;
    public static final Integer USERNAME_FIELD_BAD = 1101;
    public static final Integer USERNAME_FIELD_TOO_SHORT = 1102;
    public static final Integer USERNAME_ALREADY_EXISTS = 1103;

    public static final Integer PASSWORD_FIELD_EMPTY = 1200;
    public static final Integer PASSWORD_FIELD_BAD = 1201;

    public static final Integer USERNAME_OR_PASSWORD_WRONG = 1300;

    public static final Integer UNAUTHORIZED = 1400;
}
