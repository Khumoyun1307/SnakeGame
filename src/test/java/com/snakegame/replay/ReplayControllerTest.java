package com.snakegame.replay;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.Direction;
import com.snakegame.model.GameState;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.replay.ReplayController}.
 */
class ReplayControllerTest extends SnakeTestBase {

    private static SettingsSnapshot snapshot() {
        return new SettingsSnapshot(
                20,
                false,
                GameMode.STANDARD,
                1,
                20,
                false,
                false,
                false,
                "Replay",
                UUID.randomUUID(),
                GameSettings.Theme.RETRO,
                false,
                0,
                false,
                false
        );
    }

    @Test
    void stepOnce_appliesInputsScheduledForCurrentTickBeforeUpdate() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, true, snapshot());
            int baseTickMs = 100;
            state.setTickMs(baseTickMs);

            AtomicInteger repaints = new AtomicInteger(0);
            ReplayController controller = new ReplayController(
                    state,
                    baseTickMs,
                    List.of(new ReplayEvent(0, Direction.UP), new ReplayEvent(0, Direction.LEFT)),
                    repaints::incrementAndGet
            );

            controller.stepOnce();

            assertEquals(1, state.getTick());
            assertEquals(Direction.LEFT, state.getSnake().getDirection());
            assertEquals(1, repaints.get());
        }
    }

    @Test
    void speedMultiplier_affectsPlaybackDelayNotSimulationTickMs() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, true, snapshot());
            int baseTickMs = 120;
            state.setTickMs(baseTickMs);

            ReplayController controller = new ReplayController(state, baseTickMs, List.of(), () -> {});
            controller.setSpeedMultiplier(2.0);
            controller.stepOnce();

            assertEquals(baseTickMs, state.getTickMs(), "Simulation tickMs stays deterministic");
        }
    }
}
