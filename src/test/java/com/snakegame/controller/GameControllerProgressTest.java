package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.GameState;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import com.snakegame.util.AppPaths;
import com.snakegame.util.ProgressManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for menu/controller progress side effects.
 */
class GameControllerProgressTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetProgressManager() {
        ProgressManager.setFilePath(AppPaths.PROGRESS_FILE.toString());
        ProgressManager.clearUnlockedMaps();
    }

    private static SettingsSnapshot snapshot(GameMode mode, int selectedMapId) {
        return new SettingsSnapshot(
                20,
                false,
                mode,
                selectedMapId,
                20,
                false,
                false,
                false,
                "Tester",
                UUID.randomUUID(),
                GameSettings.Theme.RETRO,
                false,
                0,
                false,
                false
        );
    }

    @Test
    void aiMode_doesNotUnlockMaps() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setDeveloperModeEnabled(false);

            Path progress = tmp.resolve("progress.txt");
            ProgressManager.setFilePath(progress.toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();

            assertFalse(ProgressManager.isMapUnlocked(5));

            GameState state = new GameState(1L, false, snapshot(GameMode.AI, 5));
            new GameController(state, () -> {}, () -> {}, () -> {}, () -> {});

            assertFalse(ProgressManager.isMapUnlocked(5));
        }
    }

    @Test
    void mapSelectMode_unlocksSelectedMap() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setDeveloperModeEnabled(false);

            Path progress = tmp.resolve("progress.txt");
            ProgressManager.setFilePath(progress.toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();

            assertFalse(ProgressManager.isMapUnlocked(5));

            GameState state = new GameState(1L, false, snapshot(GameMode.MAP_SELECT, 5));
            new GameController(state, () -> {}, () -> {}, () -> {}, () -> {});

            assertTrue(ProgressManager.isMapUnlocked(5));
        }
    }
}

