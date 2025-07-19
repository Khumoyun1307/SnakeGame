package com.snakegame.config;

import com.snakegame.mode.GameMode;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GameSettingsTest {
    private SettingsSnapshot before;

    @BeforeEach
    void backup() {
        before = GameSettings.snapshot();
    }

    @AfterEach
    void restore() {
        GameSettings.restore(before);
    }

    @Test
    void snapshotAndRestore_preservesAllFields() {
        // Change a bunch of settings
        GameSettings.setDifficultyLevel(1);
        GameSettings.setObstaclesEnabled(true);
        GameSettings.setCurrentMode(GameMode.RACE);
        GameSettings.setSelectedMapId(9);
        GameSettings.setRaceThreshold(99);
        GameSettings.setSoundEnabled(false);
        GameSettings.setMusicEnabled(false);
        GameSettings.setShowGrid(false);
        GameSettings.setPlayerName("XYZ");
        GameSettings.setSelectedTheme(GameSettings.Theme.PIXEL_ART);
        GameSettings.setMovingObstaclesEnabled(true);
        GameSettings.setMovingObstacleCount(0);
        GameSettings.setMovingObstaclesAutoIncrement(true);
        GameSettings.setDeveloperModeEnabled(true);

        // Restore old state
        GameSettings.restore(before);

        // All values should match the snapshot
        SettingsSnapshot s = before;
        assertEquals(s.difficultyLevel(),         GameSettings.getDifficultyLevel());
        assertEquals(s.obstaclesEnabled(),       GameSettings.isObstaclesEnabled());
        assertEquals(s.currentMode(),            GameSettings.getCurrentMode());
        assertEquals(s.selectedMapId(),          GameSettings.getSelectedMapId());
        assertEquals(s.raceThreshold(),          GameSettings.getRaceThreshold());
        assertEquals(s.soundEnabled(),           GameSettings.isSoundEnabled());
        assertEquals(s.musicEnabled(),           GameSettings.isMusicEnabled());
        assertEquals(s.showGrid(),               GameSettings.isShowGrid());
        assertEquals(s.playerName(),             GameSettings.getPlayerName());
        assertEquals(s.selectedTheme(),          GameSettings.getSelectedTheme());
        assertEquals(s.movingObstaclesEnabled(), GameSettings.isMovingObstaclesEnabled());
        assertEquals(s.movingObstacleCount(),    GameSettings.getMovingObstacleCount());
        assertEquals(s.movingObstaclesAutoIncrement(),
                GameSettings.isMovingObstaclesAutoIncrement());
        assertEquals(s.developerModeEnabled(),   GameSettings.isDeveloperModeEnabled());
    }

    @Test
    void difficultyLevel_clampsBetween0And50() {
        GameSettings.setDifficultyLevel(100);
        assertEquals(50, GameSettings.getDifficultyLevel());

        GameSettings.setDifficultyLevel(-5);
        assertEquals(0, GameSettings.getDifficultyLevel());
    }

    @Test
    void speedDelayFromDifficultyLevel_calculatedCorrectly() {
        // default level is 20 → 180 - 20*2.4 = 132
        assertEquals(132, GameSettings.getSpeedDelayFromDifficultyLevel());

        GameSettings.setDifficultyLevel(50);
        assertEquals(60, GameSettings.getSpeedDelayFromDifficultyLevel());
    }

    @Test
    void setPlayerName_rejectsEmptyOrNull() {
        // snapshot current name
        String orig = GameSettings.getPlayerName();

        GameSettings.setPlayerName("");
        assertEquals(orig, GameSettings.getPlayerName());

        GameSettings.setPlayerName(null);
        assertEquals(orig, GameSettings.getPlayerName());
    }
}
