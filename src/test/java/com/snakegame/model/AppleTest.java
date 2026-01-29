package com.snakegame.model;

import com.snakegame.testutil.SnakeTestBase;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Apple}.
 */
class AppleTest extends SnakeTestBase {

    @Test
    void spawnNew_placesAppleOnGridAndWithinBounds() {
        AtomicLong tick = new AtomicLong(0);
        Apple apple = new Apple(Set.of(), new Random(123), tick::get);

        Point p = apple.getPosition();
        assertTrue(p.x >= 0 && p.x < GameConfig.SCREEN_WIDTH);
        assertTrue(p.y >= 0 && p.y < GameConfig.SCREEN_HEIGHT);
        assertEquals(0, p.x % GameConfig.UNIT_SIZE);
        assertEquals(0, p.y % GameConfig.UNIT_SIZE);
    }

    @Test
    void spawnNew_respectsForbiddenPositions() {
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;

        Point onlyAllowed = new Point(0, 0);
        Set<Point> forbidden = new HashSet<>();
        for (int gx = 0; gx < maxX; gx++) {
            for (int gy = 0; gy < maxY; gy++) {
                Point p = new Point(gx * GameConfig.UNIT_SIZE, gy * GameConfig.UNIT_SIZE);
                if (!p.equals(onlyAllowed)) forbidden.add(p);
            }
        }

        Apple apple = new Apple(forbidden, new Random(0), () -> 0L);
        assertEquals(onlyAllowed, apple.getPosition());
    }

    @Test
    void spawnRandomlyWeighted_selectsExpectedType() {
        Apple apple = new Apple(Set.of(), new Random(0), () -> 0L);

        apple.spawnRandomlyWeighted(1, 50, Set.of());
        assertEquals(AppleType.REVERSE, apple.getType());

        apple.spawnRandomlyWeighted(GameConfig.SLOWDOWN_APPLE_EVERY, 1, Set.of());
        assertEquals(AppleType.SLOWDOWN, apple.getType());

        apple.spawnRandomlyWeighted(GameConfig.GOLDEN_APPLE_EVERY, 1, Set.of());
        assertEquals(AppleType.GOLDEN, apple.getType());

        apple.spawnRandomlyWeighted(GameConfig.BIG_APPLE_EVERY, 1, Set.of());
        assertEquals(AppleType.BIG, apple.getType());

        apple.spawnRandomlyWeighted(1, 1, Set.of());
        assertEquals(AppleType.NORMAL, apple.getType());
    }

    @Test
    void isExpired_usesDeterministicTicksNotWallClock() {
        AtomicLong tick = new AtomicLong(0);
        Apple apple = new Apple(Set.of(), new Random(0), tick::get);
        apple.setTickMs(1000);
        apple.spawnNew(AppleType.BIG, Set.of());

        tick.set(5);
        assertFalse(apple.isExpired());

        tick.set(6);
        assertTrue(apple.isExpired());
    }

    @Test
    void setTickMs_clampsToAtLeastOne() {
        Apple apple = new Apple(Set.of(), new Random(0), () -> 0L);
        apple.setTickMs(0);
        apple.spawnNew(AppleType.BIG, Set.of());
        assertFalse(apple.isExpired());
    }
}
