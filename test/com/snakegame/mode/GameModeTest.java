package com.snakegame.mode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModeTest {
    @Test
    void allConstantsPresentAndOrdinalCorrect() {
        GameMode[] modes = GameMode.values();
        assertEquals(3, modes.length);
        assertEquals("STANDARD", GameMode.valueOf("STANDARD").name());
        assertEquals("MAP_SELECT", GameMode.valueOf("MAP_SELECT").name());
        assertEquals("RACE", GameMode.valueOf("RACE").name());
    }
}