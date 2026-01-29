package com.snakegame.util;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.GameSnapshot;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.util.ProgressManager}.
 */
class ProgressManagerTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetSavePath() {
        GameSaveManager.setFilePath(AppPaths.SAVE_FILE.toString());
    }

    @Test
    void load_defaultsToMap1UnlockedWhenMissing() {
        Path progress = tmp.resolve("progress.txt");
        ProgressManager.setFilePath(progress.toString());
        ProgressManager.clearUnlockedMaps();

        if (Files.exists(progress)) assertTrue(progress.toFile().delete());

        ProgressManager.load();

        assertTrue(ProgressManager.isMapUnlocked(1));
        assertTrue(Files.exists(progress));
    }

    @Test
    void unlockMap_persistsToFile() throws Exception {
        Path progress = tmp.resolve("progress.txt");
        ProgressManager.setFilePath(progress.toString());
        ProgressManager.clearUnlockedMaps();
        if (Files.exists(progress)) assertTrue(progress.toFile().delete());
        ProgressManager.load();

        ProgressManager.unlockMap(2);

        assertTrue(ProgressManager.isMapUnlocked(2));
        String content = Files.readString(progress);
        assertTrue(content.contains("1"));
        assertTrue(content.contains("2"));
    }

    @Test
    void clearSavedGameIfAllowed_preservesDeveloperOnlySaveForNormalUsers() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path savePath = tmp.resolve("savegame.txt");
            GameSaveManager.setFilePath(savePath.toString());

            GameSettings.setDeveloperModeEnabled(false);

            SettingsSnapshot ss = new SettingsSnapshot(
                    20,
                    false,
                    GameMode.MAP_SELECT,
                    88, // developer map id
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
            GameSnapshot snap = new GameSnapshot();
            snap.settingsSnapshot = ss;
            snap.mode = ss.currentMode();
            snap.selectedMapId = ss.selectedMapId();

            GameSaveManager.save(snap);
            assertTrue(GameSaveManager.hasSave());
            assertTrue(ProgressManager.isSavedGameDeveloperOnly());

            ProgressManager.clearSavedGameIfAllowed(false);
            assertTrue(GameSaveManager.hasSave(), "Developer-only saves should be preserved for non-dev sessions");
        }
    }

    @Test
    void clearSavedGameIfAllowed_clearsNormalSave() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path savePath = tmp.resolve("savegame.txt");
            GameSaveManager.setFilePath(savePath.toString());

            GameSettings.setDeveloperModeEnabled(false);

            SettingsSnapshot ss = new SettingsSnapshot(
                    20,
                    false,
                    GameMode.STANDARD,
                    1,
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
            GameSnapshot snap = new GameSnapshot();
            snap.settingsSnapshot = ss;
            snap.mode = ss.currentMode();
            snap.selectedMapId = ss.selectedMapId();

            GameSaveManager.save(snap);
            assertTrue(GameSaveManager.hasSave());
            assertFalse(ProgressManager.isSavedGameDeveloperOnly());

            ProgressManager.clearSavedGameIfAllowed(false);
            assertFalse(GameSaveManager.hasSave());
        }
    }

    @Test
    void getUnlockedMaps_returnsDefensiveCopy() {
        Path progress = tmp.resolve("progress.txt");
        ProgressManager.setFilePath(progress.toString());
        ProgressManager.clearUnlockedMaps();
        ProgressManager.load();
        ProgressManager.unlockMap(2);

        Set<Integer> copy = ProgressManager.getUnlockedMaps();
        copy.clear();
        assertTrue(ProgressManager.isMapUnlocked(1));
        assertTrue(ProgressManager.isMapUnlocked(2));
    }
}
