package com.snakegame.model;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameSnapshot}.
 */
class GameSnapshotTest extends SnakeTestBase {

    @Test
    void captureFrom_and_restore_roundTripCoreFields() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.setCurrentMode(GameMode.STANDARD);
                GameSettings.setObstaclesEnabled(false);
                GameSettings.setMovingObstaclesEnabled(false);
            });

            GameState state = new GameState(42L, false, GameSettings.snapshot());
            state.setTickMs(100);

            // Force one deterministic apple eat.
            state.getApple().setPosition(new Point(state.getSnake().getHead()));
            state.update();

            GameSnapshot snap = GameSnapshot.captureFrom(state);

            GameState restored = new GameState(123L, false, GameSettings.snapshot());
            restored.setTickMs(100);
            restored.restore(snap);

            assertEquals(snap.score, restored.getScore());
            assertEquals(snap.applesEaten, restored.getApplesEaten());
            assertEquals(snap.selectedMapId, restored.getCurrentMapId());
            assertEquals(snap.direction, restored.getSnake().getDirection());
            assertEquals(List.copyOf(snap.snakeBody), List.copyOf(restored.getSnake().getBody()));
            assertEquals(snap.appleType, restored.getApple().getType());
            assertEquals(snap.applePos, restored.getApple().getPosition());
            assertEquals(snap.appleSpawnTime, restored.getApple().getSpawnTime());
            assertEquals(snap.appleVisibleDurationMs, restored.getApple().getVisibleDurationMs());
        }
    }
}
