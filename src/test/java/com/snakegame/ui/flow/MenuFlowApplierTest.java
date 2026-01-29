package com.snakegame.ui.flow;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SettingsGuard;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.util.AppPaths;
import com.snakegame.util.GameSaveManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MenuFlowApplierTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetSavePath() {
        GameSaveManager.setFilePath(AppPaths.SAVE_FILE.toString());
    }

    @Test
    void applyStartDecision_updatesModeAndMapId() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setCurrentMode(GameMode.STANDARD);
            GameSettings.setSelectedMapId(5);

            MenuFlowApplier.applyStartDecision(new MenuFlowDecider.StartDecision(GameMode.RACE, 1, false));

            assertEquals(GameMode.RACE, GameSettings.getCurrentMode());
            assertEquals(1, GameSettings.getSelectedMapId());
        }
    }

    @Test
    void applyStartDecision_clearsSaveWhenAllowed() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path save = tmp.resolve("savegame.txt");
            GameSaveManager.setFilePath(save.toString());

            GameSaveManager.save(new com.snakegame.model.GameSnapshot());
            assertTrue(GameSaveManager.hasSave());

            MenuFlowApplier.applyStartDecision(new MenuFlowDecider.StartDecision(GameMode.STANDARD, null, true));

            assertFalse(GameSaveManager.hasSave());
        }
    }
}

