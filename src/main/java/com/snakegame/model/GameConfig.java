package com.snakegame.model;

/**
 * Centralized configuration constants for the simulation and renderer.
 *
 * <p>All dimensions are expressed in pixels unless otherwise stated. The game operates on a grid of
 * {@link #UNIT_SIZE}-sized cells within a {@link #SCREEN_WIDTH} x {@link #SCREEN_HEIGHT} playfield.</p>
 */
public class GameConfig {
    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;
    public static final int BASE_DELAY = 75;
    public static final int SLOWDOWN_OFFSET_MS = 60;

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

    // Moving obstacles

    public static final int MAX_MOVING_OBSTACLE_COUNT = 10;      // absolute cap
    public static final int DEFAULT_MOVING_OBSTACLE_COUNT = 5;   // starting default
    public static final int MIN_MOVING_OBSTACLE_LENGTH = 1;      // in units
    public static final int MAX_MOVING_OBSTACLE_LENGTH = 7;      // in units
    public static final int MOVING_OBSTACLE_SPEED = UNIT_SIZE;   // pixels per tick
    public static final int MOVING_OBSTACLE_INCREMENT_APPLES = 10;
}
