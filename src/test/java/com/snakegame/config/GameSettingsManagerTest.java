package com.snakegame.config;

import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameSettingsManager}.
 */
class GameSettingsManagerTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetSettingsPath() {
        GameSettingsManager.setFilePath(null);
    }

    @Test
    void load_createsFileIfMissing() throws IOException {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path settingsPath = tmp.resolve("settings.txt");
            GameSettingsManager.setFilePath(settingsPath.toString());

            assertFalse(Files.exists(settingsPath));

            GameSettingsManager.load();

            assertTrue(Files.exists(settingsPath));
            assertNotNull(GameSettings.getPlayerId());
        }
    }

    @Test
    void load_sanitizesInvalidUuidAndPersistsNormalizedValues() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path settingsPath = tmp.resolve("settings.txt");
            GameSettingsManager.setFilePath(settingsPath.toString());

            Files.createDirectories(settingsPath.getParent());

            Properties p = new Properties();
            p.setProperty("playerId", "not-a-uuid");
            p.setProperty("difficultyLevel", "20");
            p.setProperty("currentMode", "STANDARD");
            p.setProperty("selectedMapId", "1");
            p.setProperty("raceThreshold", "20");
            p.setProperty("soundEnabled", "true");
            p.setProperty("musicEnabled", "true");
            p.setProperty("showGrid", "true");
            p.setProperty("playerName", "Tester");
            p.setProperty("theme", "RETRO");
            p.setProperty("movingObstaclesEnabled", "false");
            p.setProperty("movingObstacleCount", "0");
            p.setProperty("movingObstaclesAutoIncrement", "false");
            p.setProperty("aiMode", "SAFE");

            try (Writer w = Files.newBufferedWriter(settingsPath, StandardCharsets.UTF_8)) {
                p.store(w, "Test");
            }

            GameSettingsManager.load();

            assertNotNull(GameSettings.getPlayerId());
            assertEquals("Tester", GameSettings.getPlayerName());

            Properties roundTrip = new Properties();
            roundTrip.load(Files.newBufferedReader(settingsPath, StandardCharsets.UTF_8));
            assertDoesNotThrow(() -> java.util.UUID.fromString(roundTrip.getProperty("playerId")));
        }
    }

    @Test
    void load_sanitizesDeveloperMapSelectionWhenDeveloperModeIsOff() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path settingsPath = tmp.resolve("settings.txt");
            GameSettingsManager.setFilePath(settingsPath.toString());

            GameSettings.setDeveloperModeEnabled(false);

            Files.createDirectories(settingsPath.getParent());

            Properties p = new Properties();
            p.setProperty("playerId", java.util.UUID.randomUUID().toString());
            p.setProperty("difficultyLevel", "20");
            p.setProperty("currentMode", GameMode.MAP_SELECT.name());
            p.setProperty("selectedMapId", "99"); // not a packaged map
            p.setProperty("raceThreshold", "20");
            p.setProperty("soundEnabled", "true");
            p.setProperty("musicEnabled", "true");
            p.setProperty("showGrid", "true");
            p.setProperty("playerName", "Tester");
            p.setProperty("theme", "RETRO");
            p.setProperty("movingObstaclesEnabled", "false");
            p.setProperty("movingObstacleCount", "0");
            p.setProperty("movingObstaclesAutoIncrement", "false");
            p.setProperty("aiMode", "SAFE");

            try (Writer w = Files.newBufferedWriter(settingsPath, StandardCharsets.UTF_8)) {
                p.store(w, "Test");
            }

            GameSettingsManager.load();

            assertEquals(1, GameSettings.getSelectedMapId());
            assertEquals(GameMode.STANDARD, GameSettings.getCurrentMode());
        }
    }
}
