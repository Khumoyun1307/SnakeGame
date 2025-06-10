package com.snakegame.model;

public class GameConfig {
    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;
    public static final int BASE_DELAY = 75;
    public static final int SLOWDOWN_OFFSET_MS = 70;

    // Power-up spawn conditions
    public static final int BIG_APPLE_EVERY = 8;
    public static final int GOLDEN_APPLE_EVERY = 17;
    public static final int SLOWDOWN_APPLE_EVERY = 25;
    public static final int REVERSE_APPLE_EVERY_SCORE = 50;

    // Power-up durations
    public static final int GOLDEN_DURATION_MS = 10000;
    public static final int SLOWDOWN_DURATION_MS = 8000;
    public static final int REVERSE_DURATION_MS = 8000;

    public static final int BIG_VISIBLE_DURATION_MS = 6000;
    public static final int GOLDEN_VISIBLE_DURATION_MS = 10000;
    public static final int SLOWDOWN_VISIBLE_DURATION_MS = 10000;
}
