package com.snakegame.controller.input;

import com.snakegame.ai.AiMode;
import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameState;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.controller.input.AiDirectionProvider}.
 */
class AiDirectionProviderTest {

    private static SettingsSnapshot snapshot(GameMode mode) {
        return new SettingsSnapshot(
                20,
                false,
                mode,
                1,
                20,
                false,
                false,
                false,
                "AI",
                UUID.randomUUID(),
                GameSettings.Theme.RETRO,
                false,
                0,
                false,
                false
        );
    }

    @Test
    void chaseMode_movesTowardAppleWhenSafe() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, false, snapshot(GameMode.STANDARD));
            state.setTickMs(100);

            Point head = state.getSnake().getHead();
            state.getSnake().setDirection(Direction.RIGHT);
            state.getApple().setPosition(new Point(head.x + GameConfig.UNIT_SIZE, head.y));

            AiDirectionProvider provider = new AiDirectionProvider(AiMode.CHASE);
            assertEquals(Direction.RIGHT, provider.nextDirection(state));
        }
    }

    @Test
    void chaseMode_doesNotReverseIntoOppositeDirection() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, false, snapshot(GameMode.STANDARD));
            state.setTickMs(100);

            Point head = state.getSnake().getHead();
            state.getSnake().setDirection(Direction.RIGHT);
            state.getApple().setPosition(new Point(head.x - GameConfig.UNIT_SIZE, head.y));

            AiDirectionProvider provider = new AiDirectionProvider(AiMode.CHASE);
            Direction chosen = provider.nextDirection(state);
            assertNotNull(chosen);
            assertFalse(chosen.isOpposite(state.getSnake().getDirection()));
        }
    }

    @Test
    void safeMode_prefersAppleWithoutBreakingBasicConstraints() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, false, snapshot(GameMode.STANDARD));
            state.setTickMs(100);

            Point head = state.getSnake().getHead();
            state.getSnake().setDirection(Direction.RIGHT);
            state.getApple().setPosition(new Point(head.x + GameConfig.UNIT_SIZE, head.y));

            AiDirectionProvider provider = new AiDirectionProvider(AiMode.SAFE);
            Direction chosen = provider.nextDirection(state);
            assertEquals(Direction.RIGHT, chosen);
        }
    }

    @Test
    void survivalMode_returnsAValidNonOppositeMoveInOpenField() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, false, snapshot(GameMode.STANDARD));
            state.setTickMs(100);

            state.getSnake().setDirection(Direction.RIGHT);
            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE)); // off-path, shouldn't matter

            AiDirectionProvider provider = new AiDirectionProvider(AiMode.SURVIVAL);
            Direction chosen = provider.nextDirection(state);

            assertNotNull(chosen);
            assertFalse(chosen.isOpposite(state.getSnake().getDirection()));
        }
    }
}
