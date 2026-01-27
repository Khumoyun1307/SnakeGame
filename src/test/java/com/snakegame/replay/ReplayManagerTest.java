package com.snakegame.replay;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.Direction;
import com.snakegame.testutil.FileBackups;
import com.snakegame.testutil.SettingsGuard;
import com.snakegame.util.AppPaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.replay.ReplayManager}.
 */
class ReplayManagerTest {

    private static final Path LAST_PATH = AppPaths.REPLAY_LAST_FILE;
    private static final Path BEST_PATH = AppPaths.REPLAY_BEST_FILE;

    private static SettingsSnapshot settingsSnapshot() {
        return new SettingsSnapshot(
                20,
                false,
                GameMode.STANDARD,
                1,
                20,
                true,
                true,
                true,
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
    void saveLast_and_loadLast_roundTrip() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard();
             FileBackups backups = new FileBackups(LAST_PATH, BEST_PATH)) {

            Files.deleteIfExists(LAST_PATH);
            Files.deleteIfExists(BEST_PATH);

            ReplayData d = new ReplayData();
            d.seed = 123L;
            d.finalScore = 7;
            d.runSettingsSnapshot = settingsSnapshot();
            d.startMapId = 1;
            d.events = List.of(new ReplayEvent(0, Direction.UP), new ReplayEvent(3, Direction.LEFT));

            ReplayManager.saveLast(d);
            Optional<ReplayData> loaded = ReplayManager.loadLast();
            assertTrue(loaded.isPresent());

            ReplayData r = loaded.get();
            assertEquals(d.seed, r.seed);
            assertEquals(d.finalScore, r.finalScore);
            assertEquals(d.startMapId, r.startMapId);
            assertEquals(2, r.events.size());
            assertEquals(Direction.UP, r.events.get(0).direction);
        }
    }

    @Test
    void saveBestIfHigher_onlyOverwritesWhenScoreImproves() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard();
             FileBackups backups = new FileBackups(LAST_PATH, BEST_PATH)) {

            Files.deleteIfExists(BEST_PATH);

            ReplayData best = new ReplayData();
            best.seed = 1L;
            best.finalScore = 10;
            best.runSettingsSnapshot = settingsSnapshot();
            best.startMapId = 1;

            ReplayManager.saveBestIfHigher(best);
            assertTrue(ReplayManager.hasBest());

            ReplayData worse = new ReplayData();
            worse.seed = 2L;
            worse.finalScore = 3;
            worse.runSettingsSnapshot = settingsSnapshot();
            worse.startMapId = 1;

            ReplayManager.saveBestIfHigher(worse);
            ReplayData loaded = ReplayManager.loadBest().orElseThrow();
            assertEquals(10, loaded.finalScore);
        }
    }
}
