package com.snakegame.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class AppleTypeTest {

    @Test
    void scoreValuesAndColors_areAsConfigured() {
        assertAll("NORMAL",
                () -> assertEquals(1, AppleType.NORMAL.getScoreValue()),
                () -> assertEquals(Color.RED, AppleType.NORMAL.getColor())
        );
        assertAll("BIG",
                () -> assertEquals(4, AppleType.BIG.getScoreValue()),
                () -> assertEquals(Color.ORANGE, AppleType.BIG.getColor())
        );
        assertAll("GOLDEN",
                () -> assertEquals(2, AppleType.GOLDEN.getScoreValue()),
                () -> assertEquals(Color.YELLOW, AppleType.GOLDEN.getColor())
        );
        assertAll("SLOWDOWN",
                () -> assertEquals(1, AppleType.SLOWDOWN.getScoreValue()),
                () -> assertEquals(Color.CYAN, AppleType.SLOWDOWN.getColor())
        );
        assertAll("REVERSE",
                () -> assertEquals(1, AppleType.REVERSE.getScoreValue()),
                () -> assertEquals(Color.MAGENTA, AppleType.REVERSE.getColor())
        );
    }
}
