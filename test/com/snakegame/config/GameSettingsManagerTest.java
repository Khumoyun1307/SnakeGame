package com.snakegame.config;

import com.snakegame.mode.GameMode;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class GameSettingsManagerTest {
    private static final Path SETTINGS_PATH = Paths.get("data/settings.txt");
    private SettingsSnapshot before;
    private byte[] originalBytes;

    @BeforeEach
    void backupAndClear() throws IOException {
        // 1) Snapshot all mutable settings
        before = GameSettings.snapshot();

        // 2) Backup the existing file (if any)
        if (Files.exists(SETTINGS_PATH)) {
            originalBytes = Files.readAllBytes(SETTINGS_PATH);
        }

        // 3) Delete to force a fresh load/save
        Files.deleteIfExists(SETTINGS_PATH);
    }

    @AfterEach
    void restore() throws IOException {
        // 1) Restore in‐memory settings
        GameSettings.restore(before);

        // 2) Restore or delete the file
        if (originalBytes != null) {
            Files.createDirectories(SETTINGS_PATH.getParent());
            Files.write(SETTINGS_PATH, originalBytes);
        } else {
            Files.deleteIfExists(SETTINGS_PATH);
        }
    }

    @Test
    void save_writesAllProperties() throws IOException {
        // Mutate every setting
        GameSettings.setDifficultyLevel(30);
        GameSettings.setObstaclesEnabled(true);
        GameSettings.setCurrentMode(GameMode.RACE);
        GameSettings.setSelectedMapId(5);
        GameSettings.setRaceThreshold(42);
        GameSettings.setSoundEnabled(false);
        GameSettings.setMusicEnabled(false);
        GameSettings.setShowGrid(false);
        GameSettings.setPlayerName("Tester");
        GameSettings.setSelectedTheme(GameSettings.Theme.NEON);
        GameSettings.setMovingObstaclesEnabled(true);
        GameSettings.setMovingObstacleCount(2);
        GameSettings.setMovingObstaclesAutoIncrement(true);

        // After any setter, GameSettingsManager.save() has run, so file must exist
        assertTrue(Files.exists(SETTINGS_PATH), "Settings file must be created");

        // Load raw properties
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(SETTINGS_PATH)) {
            props.load(reader);
        }

        // Verify
        assertEquals("30",   props.getProperty("difficultyLevel"));
        assertEquals("true", props.getProperty("obstaclesEnabled"));
        assertEquals("RACE", props.getProperty("currentMode"));
        assertEquals("5",    props.getProperty("selectedMapId"));
        assertEquals("42",   props.getProperty("raceThreshold"));
        assertEquals("false", props.getProperty("soundEnabled"));
        assertEquals("false", props.getProperty("musicEnabled"));
        assertEquals("false", props.getProperty("showGrid"));
        assertEquals("Tester", props.getProperty("playerName"));
        assertEquals("NEON",   props.getProperty("theme"));
        assertEquals("true",   props.getProperty("movingObstaclesEnabled"));
        assertEquals("2",      props.getProperty("movingObstacleCount"));
        assertEquals("true",   props.getProperty("movingObstaclesAutoIncrement"));
    }

    @Test
    void load_readsPropertiesIntoStaticFields() throws IOException {
        // Write a custom settings file
        Files.createDirectories(SETTINGS_PATH.getParent());
        try (var writer = Files.newBufferedWriter(SETTINGS_PATH)) {
            writer.write("difficultyLevel=7\n");
            writer.write("obstaclesEnabled=true\n");
            writer.write("currentMode=MAP_SELECT\n");
            writer.write("selectedMapId=3\n");
            writer.write("raceThreshold=15\n");
            writer.write("soundEnabled=false\n");
            writer.write("musicEnabled=true\n");
            writer.write("showGrid=false\n");
            writer.write("playerName=LoadTest\n");
            writer.write("theme=PIXEL_ART\n");
            writer.write("movingObstaclesEnabled=true\n");
            writer.write("movingObstacleCount=4\n");
            writer.write("movingObstaclesAutoIncrement=true\n");
        }

        // Perform the load
        GameSettingsManager.load();

        // Assert that GameSettings reflects the file
        assertEquals(7,  GameSettings.getDifficultyLevel());
        assertTrue(GameSettings.isObstaclesEnabled());
        assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
        assertEquals(3,  GameSettings.getSelectedMapId());
        assertEquals(15, GameSettings.getRaceThreshold());
        assertFalse(GameSettings.isSoundEnabled());
        assertTrue(GameSettings.isMusicEnabled());
        assertFalse(GameSettings.isShowGrid());
        assertEquals("LoadTest", GameSettings.getPlayerName());
        assertEquals(GameSettings.Theme.PIXEL_ART, GameSettings.getSelectedTheme());
        assertTrue(GameSettings.isMovingObstaclesEnabled());
        assertEquals(4, GameSettings.getMovingObstacleCount());
        assertTrue(GameSettings.isMovingObstaclesAutoIncrement());
    }
}
