package com.snakegame.util;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.*;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Point;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.util.GameSaveManager}.
 */
class GameSaveManagerTest {

    @TempDir
    Path tmp;

    @AfterEach
    void resetSavePath() {
        GameSaveManager.setFilePath(AppPaths.SAVE_FILE.toString());
    }

    @Test
    void save_and_load_roundTripCoreFields() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path savePath = tmp.resolve("savegame.txt");
            GameSaveManager.setFilePath(savePath.toString());

            SettingsSnapshot ss = new SettingsSnapshot(
                    20,
                    true,
                    GameMode.RACE,
                    3,
                    7,
                    true,
                    true,
                    true,
                    "Tester",
                    UUID.randomUUID(),
                    GameSettings.Theme.NEON,
                    true,
                    2,
                    true,
                    false
            );

            GameSnapshot snap = new GameSnapshot();
            snap.settingsSnapshot = ss;
            snap.mode = ss.currentMode();
            snap.selectedMapId = ss.selectedMapId();

            snap.score = 123;
            snap.applesEaten = 9;
            snap.direction = Direction.DOWN;
            snap.snakeBody = List.of(new Point(0, 0), new Point(25, 0), new Point(50, 0));

            snap.applePos = new Point(100, 125);
            snap.appleType = AppleType.GOLDEN;
            snap.appleSpawnTime = 111L;
            snap.appleVisibleDurationMs = GameConfig.GOLDEN_VISIBLE_DURATION_MS;

            snap.doubleScoreActive = true;
            snap.doubleScoreEndTime = 50;
            snap.slowed = true;
            snap.slowEndTime = 60;
            snap.reversedControls = true;
            snap.reverseEndTime = 70;

            snap.obstacles = List.of(new Point(25, 25), new Point(50, 50));
            snap.movingObstacles = List.of(new MovingObstacleSnapshot(List.of(new Point(0, 25)), 25, 0));

            GameSaveManager.save(snap);
            Optional<GameSnapshot> loaded = GameSaveManager.load();

            assertTrue(loaded.isPresent());
            GameSnapshot s = loaded.get();

            assertEquals(GameMode.RACE, s.mode);
            assertEquals(3, s.selectedMapId);
            assertEquals(123, s.score);
            assertEquals(9, s.applesEaten);
            assertEquals(Direction.DOWN, s.direction);
            assertEquals(snap.snakeBody, s.snakeBody);
            assertEquals(new Point(100, 125), s.applePos);
            assertEquals(AppleType.GOLDEN, s.appleType);
            assertEquals(111L, s.appleSpawnTime);
            assertEquals(GameConfig.GOLDEN_VISIBLE_DURATION_MS, s.appleVisibleDurationMs);
            assertEquals(snap.obstacles, s.obstacles);
            assertEquals(1, s.movingObstacles.size());
            assertEquals(25, s.movingObstacles.get(0).dx);
        }
    }

    @Test
    void load_whenCorrupted_clearsSaveAndReturnsEmpty() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path savePath = tmp.resolve("savegame.txt");
            GameSaveManager.setFilePath(savePath.toString());

            Files.createDirectories(savePath.getParent());
            Files.writeString(savePath, "score=not-a-number\n", StandardCharsets.UTF_8);
            assertTrue(GameSaveManager.hasSave());

            Logger logger = Logger.getLogger(GameSaveManager.class.getName());
            Level prevLevel = logger.getLevel();
            try {
                logger.setLevel(Level.OFF); // corrupted-save logging is expected in this test
                Optional<GameSnapshot> loaded = GameSaveManager.load();
                assertTrue(loaded.isEmpty());
            } finally {
                logger.setLevel(prevLevel);
            }
            assertFalse(GameSaveManager.hasSave(), "Corrupted save should be cleared");
        }
    }
}
