package com.snakegame.model;

import java.awt.*;
import java.util.Random;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * Represents the current apple in the world, including its type and deterministic visibility window.
 *
 * <p>To support reliable replays and stable tests, time-based behavior (such as apple expiration) is
 * driven by a tick counter via {@link LongSupplier} instead of wall clock time. Wall-clock time is
 * still tracked for UI-only animation (pulsing).</p>
 */
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
    /**
     * Creates a new apple with a non-deterministic random source (backward compatible).
     *
     * @param forbidden positions that cannot be used for spawning (snake body, obstacles, etc.)
     */
    public Apple(Set<Point> forbidden) {
        this(forbidden, new Random(), () -> 0L);
    }

    // Deterministic constructor
    /**
     * Creates a new apple using the provided RNG and tick source for deterministic behavior.
     *
     * @param forbidden positions that cannot be used for spawning (snake body, obstacles, etc.)
     * @param rng random source used to choose spawn coordinates
     * @param tickSupplier deterministic tick counter used for expiration logic
     */
    public Apple(Set<Point> forbidden, Random rng, LongSupplier tickSupplier) {
        this.rng = rng;
        this.tickSupplier = tickSupplier;
        spawnNew(AppleType.NORMAL, forbidden);
    }

    /**
     * Sets the duration (in milliseconds) represented by a single simulation tick.
     *
     * <p>This value is used to convert configured millisecond durations into tick counts so that
     * expiration remains deterministic under variable frame/timer delays.</p>
     *
     * @param tickMs tick duration in milliseconds (values &lt;= 0 are clamped to 1)
     */
    public void setTickMs(int tickMs) {
        this.tickMs = Math.max(1, tickMs);
    }

    private long ticksFromMs(long ms) {
        if (ms <= 0) return 0;
        return (ms + tickMs - 1L) / tickMs; // ceil
    }

    /**
     * Spawns a new apple of the given type at a random, unoccupied location.
     *
     * @param type apple type to spawn
     * @param forbiddenPositions positions that cannot be selected for spawning
     */
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
    /**
     * Returns whether this apple's visible window has expired.
     *
     * <p>Expiration is based on the simulation tick counter and remains stable across replays.</p>
     *
     * @return {@code true} if the apple should be considered expired
     */
    public boolean isExpired() {
        if (visibleDurationTicks <= 0) return false;
        long nowTick = tickSupplier.getAsLong();
        return (nowTick - spawnTick) >= visibleDurationTicks;
    }

    /**
     * Spawns an apple type based on score/apples-eaten thresholds.
     *
     * @param applesEaten number of apples eaten so far in the run
     * @param score current score
     * @param forbidden positions that cannot be used for spawning
     */
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
    /**
     * Restores apple state from a snapshot.
     *
     * <p>This method keeps timing data compatible with older snapshots which stored visibility in
     * milliseconds. For deterministic logic on restored games, the "spawn tick" is reset to the
     * current tick.</p>
     *
     * @param position restored apple position (pixel coordinates)
     * @param type restored apple type
     * @param spawnTimeMillis UI-only wall-clock timestamp used for pulsing animation
     * @param visibleDurationMs visible duration in milliseconds (snapshot-compatible)
     */
    public void restore(Point position, AppleType type, long spawnTimeMillis, long visibleDurationMs) {
        this.position = new Point(position);
        this.type = type;
        this.spawnTimeMillis = spawnTimeMillis;

        // For deterministic logic on restored games, best effort:
        // we can't reconstruct the original tick from millis without saved tick, so reset to "now"
        this.spawnTick = tickSupplier.getAsLong();
        this.visibleDurationTicks = ticksFromMs(visibleDurationMs);
    }

    /**
     * Returns the current apple position (pixel coordinates).
     *
     * @return apple position in pixels
     */
    public Point getPosition() { return position; }
    /**
     * Updates the current apple position (pixel coordinates).
     *
     * @param position new position in pixels
     */
    public void setPosition(Point position) { this.position = position; }
    /**
     * Returns the current apple type.
     *
     * @return apple type
     */
    public AppleType getType() { return type; }

    /** UI pulse uses millis */
    /**
     * Returns the wall-clock time captured when the apple last spawned (UI-only).
     *
     * @return spawn time in milliseconds since epoch
     */
    public long getSpawnTime() { return spawnTimeMillis; }

    /** kept in ms for UI + snapshot compatibility */
    /**
     * Returns the apple's configured visible duration in milliseconds.
     *
     * @return visible duration in milliseconds, or 0 if the apple does not expire
     */
    public long getVisibleDurationMs() {
        return switch (type) {
            case BIG -> GameConfig.BIG_VISIBLE_DURATION_MS;
            case GOLDEN -> GameConfig.GOLDEN_VISIBLE_DURATION_MS;
            case SLOWDOWN -> GameConfig.SLOWDOWN_VISIBLE_DURATION_MS;
            default -> 0;
        };
    }
}
