package com.snakegame.model;

import java.awt.*;
import java.util.Random;
import java.util.Set;
import java.util.function.LongSupplier;

public class Apple {
    private final Random rng;
    private final LongSupplier tickSupplier; // deterministic "time" for logic

    private Point position;
    private AppleType type;

    // For UI animation only (pulse etc.)
    private long spawnTimeMillis;

    // Deterministic timing for expiration
    private long spawnTick;
    private long visibleDurationTicks;

    // Needed to convert ms config -> ticks
    private int tickMs = 100;

    // Backward-compatible constructor (non-deterministic seed per run)
    public Apple(Set<Point> forbidden) {
        this(forbidden, new Random(), () -> 0L);
    }

    // Deterministic constructor
    public Apple(Set<Point> forbidden, Random rng, LongSupplier tickSupplier) {
        this.rng = rng;
        this.tickSupplier = tickSupplier;
        spawnNew(AppleType.NORMAL, forbidden);
    }

    public void setTickMs(int tickMs) {
        this.tickMs = Math.max(1, tickMs);
    }

    private long ticksFromMs(long ms) {
        if (ms <= 0) return 0;
        return (ms + tickMs - 1L) / tickMs; // ceil
    }

    public void spawnNew(AppleType type, Set<Point> forbiddenPositions) {
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;

        Point newPos;
        do {
            int x = rng.nextInt(maxX) * GameConfig.UNIT_SIZE;
            int y = rng.nextInt(maxY) * GameConfig.UNIT_SIZE;
            newPos = new Point(x, y);
        } while (forbiddenPositions.contains(newPos));

        this.position = newPos;
        this.type = type;

        // UI animation timestamp
        this.spawnTimeMillis = System.currentTimeMillis();

        // deterministic timing
        this.spawnTick = tickSupplier.getAsLong();

        long visibleMs = switch (type) {
            case BIG -> GameConfig.BIG_VISIBLE_DURATION_MS;
            case GOLDEN -> GameConfig.GOLDEN_VISIBLE_DURATION_MS;
            case SLOWDOWN -> GameConfig.SLOWDOWN_VISIBLE_DURATION_MS;
            default -> 0;
        };
        this.visibleDurationTicks = ticksFromMs(visibleMs);
    }

    /** Deterministic expiration: based on tick count, NOT wall clock. */
    public boolean isExpired() {
        if (visibleDurationTicks <= 0) return false;
        long nowTick = tickSupplier.getAsLong();
        return (nowTick - spawnTick) >= visibleDurationTicks;
    }

    public void spawnRandomlyWeighted(int applesEaten, int score, Set<Point> forbidden) {
        if (score > 0 && score % GameConfig.REVERSE_APPLE_EVERY_SCORE == 0) {
            spawnNew(AppleType.REVERSE, forbidden);
        } else if (applesEaten > 0 && applesEaten % GameConfig.SLOWDOWN_APPLE_EVERY == 0) {
            spawnNew(AppleType.SLOWDOWN, forbidden);
        } else if (applesEaten > 0 && applesEaten % GameConfig.GOLDEN_APPLE_EVERY == 0) {
            spawnNew(AppleType.GOLDEN, forbidden);
        } else if (applesEaten > 0 && applesEaten % GameConfig.BIG_APPLE_EVERY == 0) {
            spawnNew(AppleType.BIG, forbidden);
        } else {
            spawnNew(AppleType.NORMAL, forbidden);
        }
    }

    // Keep restore as-is so GameSnapshot still works
    public void restore(Point position, AppleType type, long spawnTimeMillis, long visibleDurationMs) {
        this.position = new Point(position);
        this.type = type;
        this.spawnTimeMillis = spawnTimeMillis;

        // For deterministic logic on restored games, best effort:
        // we can't reconstruct the original tick from millis without saved tick, so reset to "now"
        this.spawnTick = tickSupplier.getAsLong();
        this.visibleDurationTicks = ticksFromMs(visibleDurationMs);
    }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }
    public AppleType getType() { return type; }

    /** UI pulse uses millis */
    public long getSpawnTime() { return spawnTimeMillis; }

    /** kept in ms for UI + snapshot compatibility */
    public long getVisibleDurationMs() {
        return switch (type) {
            case BIG -> GameConfig.BIG_VISIBLE_DURATION_MS;
            case GOLDEN -> GameConfig.GOLDEN_VISIBLE_DURATION_MS;
            case SLOWDOWN -> GameConfig.SLOWDOWN_VISIBLE_DURATION_MS;
            default -> 0;
        };
    }
}
