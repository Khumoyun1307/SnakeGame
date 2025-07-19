package com.snakegame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppleTest {

    private Apple apple;

    @BeforeEach
    void setUp() {
        // start with no forbidden positions
        apple = new Apple(Set.of());
    }

    @Test
    void spawnNew_setsTypeAndVisibleDuration() {
        for (AppleType type : AppleType.values()) {
            apple.spawnNew(type, Set.of());
            assertEquals(type, apple.getType(), "type should be set");
            long expectedDur;
            switch (type) {
                case BIG     -> expectedDur = GameConfig.BIG_VISIBLE_DURATION_MS;
                case GOLDEN -> expectedDur = GameConfig.GOLDEN_VISIBLE_DURATION_MS;
                case SLOWDOWN -> expectedDur = GameConfig.SLOWDOWN_VISIBLE_DURATION_MS;
                default -> expectedDur = 0;
            }
            assertEquals(expectedDur, apple.getVisibleDurationMs(),
                    "visibleDuration must match config for " + type);
            assertTrue(apple.getSpawnTime() > 0, "spawnTime should be set");
        }
    }

    @Test
    void spawnNew_positionWithinBounds_andNotForbidden() {
        var forbidden = Set.of(
                new Point(0, 0),
                new Point(GameConfig.UNIT_SIZE, 0)
        );
        apple.spawnNew(AppleType.NORMAL, forbidden);
        Point p = apple.getPosition();
        // within screen
        assertTrue(p.x >= 0 && p.x < GameConfig.SCREEN_WIDTH);
        assertTrue(p.y >= 0 && p.y < GameConfig.SCREEN_HEIGHT);
        // aligned to grid
        assertEquals(0, p.x % GameConfig.UNIT_SIZE);
        assertEquals(0, p.y % GameConfig.UNIT_SIZE);
        // not forbidden
        assertFalse(forbidden.contains(p));
    }

    @Test
    void isExpired_neverForNormalType() {
        apple.spawnNew(AppleType.NORMAL, Set.of());
        assertFalse(apple.isExpired());
    }

    @Test
    void isExpired_trueAfterDurationElapsed() throws Exception {
        apple.spawnNew(AppleType.BIG, Set.of());
        long dur = apple.getVisibleDurationMs();
        // force spawnTime into the past via reflection
        Field f = Apple.class.getDeclaredField("spawnTime");
        f.setAccessible(true);
        f.setLong(apple, System.currentTimeMillis() - dur - 1);

        assertTrue(apple.isExpired(), "should be expired once past visibleDuration");
    }

    @Test
    void spawnRandomlyWeighted_picksCorrectType() {
        // 1) score triggers REVERSE first
        apple.spawnRandomlyWeighted(0, GameConfig.REVERSE_APPLE_EVERY_SCORE, Set.of());
        assertEquals(AppleType.REVERSE, apple.getType());

        // 2) applesEaten triggers SLOWDOWN
        apple.spawnRandomlyWeighted(GameConfig.SLOWDOWN_APPLE_EVERY, 0, Set.of());
        assertEquals(AppleType.SLOWDOWN, apple.getType());

        // 3) applesEaten triggers GOLDEN
        apple.spawnRandomlyWeighted(GameConfig.GOLDEN_APPLE_EVERY, 0, Set.of());
        assertEquals(AppleType.GOLDEN, apple.getType());

        // 4) applesEaten triggers BIG
        apple.spawnRandomlyWeighted(GameConfig.BIG_APPLE_EVERY, 0, Set.of());
        assertEquals(AppleType.BIG, apple.getType());

        // 5) neither condition → NORMAL
        apple.spawnRandomlyWeighted(1, 1, Set.of());
        assertEquals(AppleType.NORMAL, apple.getType());
    }
}