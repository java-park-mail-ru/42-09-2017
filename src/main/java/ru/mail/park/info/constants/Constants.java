package ru.mail.park.info.constants;

public class Constants {
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String USERNAME_REGEXP = "([A-Za-z][-_A-Za-z0-9]*)?";
    public static final String SESSION_ATTR = "user_info";

    public static final int MAPS_ON_PAGE = 10;

    // Box2d constants
    public static final long TIMEOUT = 30L;
    public static final float GRAVITY_X = 0f;
    public static final float GRAVITY_Y = -10f;
    public static final float DELTA = 1 / 60f;
    public static final int VEL_ITER = 10;
    public static final int POS_ITER = 10;
    public static final int SECOND = 1000000000;
    public static final int MICRO_SECOND = 1000000;
    public static final int FPS = 60;

    public static final float ALLOWED_POS_DELTA = 0.05f;
    public static final float ALLOWED_VEL_DELTA = 0.05f;
    public static final float ALLOWED_ANGLE_DELTA = 0.05f;
}
