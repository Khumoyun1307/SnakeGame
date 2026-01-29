package com.snakegame.model;

import com.snakegame.testutil.SnakeTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Direction}.
 */
class DirectionTest extends SnakeTestBase {

    @ParameterizedTest
    @EnumSource(Direction.class)
    void isOpposite_isNeverOppositeOfSelf(Direction d) {
        assertFalse(d.isOpposite(d));
    }

    @Test
    void isOpposite_isSymmetric() {
        for (Direction a : Direction.values()) {
            for (Direction b : Direction.values()) {
                assertEquals(a.isOpposite(b), b.isOpposite(a));
            }
        }
    }

    @Test
    void isOpposite_matchesExpectedPairs() {
        assertTrue(Direction.UP.isOpposite(Direction.DOWN));
        assertTrue(Direction.DOWN.isOpposite(Direction.UP));
        assertTrue(Direction.LEFT.isOpposite(Direction.RIGHT));
        assertTrue(Direction.RIGHT.isOpposite(Direction.LEFT));

        assertFalse(Direction.UP.isOpposite(Direction.LEFT));
        assertFalse(Direction.UP.isOpposite(Direction.RIGHT));
        assertFalse(Direction.DOWN.isOpposite(Direction.LEFT));
        assertFalse(Direction.DOWN.isOpposite(Direction.RIGHT));
    }
}
