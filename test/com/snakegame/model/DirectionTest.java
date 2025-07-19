package com.snakegame.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void isOpposite_returnsTrueForOppositePairs() {
        assertTrue(Direction.UP.isOpposite(Direction.DOWN));
        assertTrue(Direction.DOWN.isOpposite(Direction.UP));
        assertTrue(Direction.LEFT.isOpposite(Direction.RIGHT));
        assertTrue(Direction.RIGHT.isOpposite(Direction.LEFT));
    }

    @Test
    void isOpposite_returnsFalseForNonOppositePairs() {
        assertFalse(Direction.UP.isOpposite(Direction.LEFT));
        assertFalse(Direction.UP.isOpposite(Direction.RIGHT));
        assertFalse(Direction.DOWN.isOpposite(Direction.LEFT));
        assertFalse(Direction.DOWN.isOpposite(Direction.RIGHT));
        assertFalse(Direction.LEFT.isOpposite(Direction.UP));
        assertFalse(Direction.LEFT.isOpposite(Direction.DOWN));
        assertFalse(Direction.RIGHT.isOpposite(Direction.UP));
        assertFalse(Direction.RIGHT.isOpposite(Direction.DOWN));
    }
}