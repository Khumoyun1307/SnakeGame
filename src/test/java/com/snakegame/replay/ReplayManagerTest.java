package com.snakegame.replay;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.Direction;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.replay.ReplayManager}.
 */
class ReplayManagerTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetReplayPaths() {
        ReplayManager.setLastPath(null);
        ReplayManager.setBestPath(null);
    }

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
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path lastPath = tmp.resolve("replay_last.txt");
            Path bestPath = tmp.resolve("replay_best.txt");
            ReplayManager.setLastPath(lastPath.toString());
            ReplayManager.setBestPath(bestPath.toString());

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
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path lastPath = tmp.resolve("replay_last.txt");
            Path bestPath = tmp.resolve("replay_best.txt");
            ReplayManager.setLastPath(lastPath.toString());
            ReplayManager.setBestPath(bestPath.toString());

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
