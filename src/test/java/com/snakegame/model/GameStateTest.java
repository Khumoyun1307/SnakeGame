package com.snakegame.model;

import com.snakegame.ai.AiMode;
import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameState}.
 */
class GameStateTest {

    private static SettingsSnapshot snapshot(GameMode mode,
                                            int selectedMapId,
                                            int raceThreshold,
                                            boolean obstacles,
                                            boolean moving,
                                            int movingCount,
                                            boolean movingAuto) {
        return new SettingsSnapshot(
                20,
                obstacles,
                mode,
                selectedMapId,
                raceThreshold,
                false,
                false,
                false,
                "Test",
                UUID.randomUUID(),
                GameSettings.Theme.RETRO,
                moving,
                movingCount,
                movingAuto,
                false
        );
    }

    @Test
    void update_wrapsAroundEdges() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(123L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(100);

            // Place head at far right edge so next move wraps.
            Point head = state.getSnake().getHead();
            head.x = GameConfig.SCREEN_WIDTH - GameConfig.UNIT_SIZE;
            head.y = 0;
            state.getSnake().setDirection(Direction.RIGHT);

            // Keep apple off the snake's row so it won't be eaten.
            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE));

            state.update();
            assertEquals(0, state.getSnake().getHead().x);
            assertEquals(0, state.getSnake().getHead().y);
        }
    }

    @Test
    void update_emitsAppleEatenAndUpdatesScore() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(0L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(100);

            state.getApple().spawnNew(AppleType.NORMAL, Set.of());
            state.getApple().setPosition(new Point(state.getSnake().getHead()));

            state.update();
            List<GameEvent> events = state.consumeEvents();
            assertTrue(events.stream().anyMatch(e -> e instanceof GameEvent.AppleEaten));
            assertEquals(1, state.getApplesEaten());
            assertEquals(1, state.getScore());
        }
    }

    @Test
    void goldenApple_enablesDoubleScoreAndDoublesNextApple() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(0L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(100);

            Set<Point> forbidden = new HashSet<>(state.getSnake().getBody());
            state.getApple().spawnNew(AppleType.GOLDEN, forbidden);
            state.getApple().setPosition(new Point(state.getSnake().getHead()));

            state.update();
            assertTrue(state.isDoubleScoreActive());
            assertEquals(2, state.getScore(), "Golden apple scoreValue is 2");

            // Next apple: normal, but place it on head again to force eat.
            state.getApple().setPosition(new Point(state.getSnake().getHead()));
            state.update();

            assertEquals(4, state.getScore(), "Normal apple (1) doubled => +2");
        }
    }

    @Test
    void effects_expireByTick() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(0L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(100);

            Set<Point> forbidden = new HashSet<>(state.getSnake().getBody());
            state.getApple().spawnNew(AppleType.GOLDEN, forbidden);
            state.getApple().setPosition(new Point(state.getSnake().getHead()));
            state.update();

            long endTick = state.getDoubleScoreEndTime();
            assertTrue(state.isDoubleScoreActive());

            // Keep apple off-path so we don't accidentally eat during the wait.
            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE));

            while (state.getTick() < endTick) {
                state.update();
            }
            assertFalse(state.isDoubleScoreActive());
        }
    }

    @Test
    void bigApple_expiresAndRespawnsAsNormal() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(0L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(GameConfig.BIG_VISIBLE_DURATION_MS); // => 1 tick visibility

            Set<Point> forbidden = new HashSet<>(state.getSnake().getBody());
            state.getApple().spawnNew(AppleType.BIG, forbidden);
            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE)); // not on head

            state.update();
            assertEquals(AppleType.NORMAL, state.getApple().getType());
        }
    }

    @Test
    void selfCollision_setsGameOverEvent() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(0L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(100);

            Snake snake = Snake.fromBody(List.of(
                    new Point(50, 50),
                    new Point(25, 50),
                    new Point(25, 75),
                    new Point(50, 75),
                    new Point(75, 75),
                    new Point(75, 50)
            ), Direction.DOWN);
            com.snakegame.testutil.Reflect.setField(state, "snake", snake);
            state.getApple().setPosition(new Point(0, 0));

            state.update();

            assertFalse(state.isRunning());
            assertTrue(state.consumeEvents().stream().anyMatch(e -> e instanceof GameEvent.GameOver));
        }
    }

    @Test
    void obstacleCollision_setsGameOverEvent() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(0L, false, snapshot(GameMode.STANDARD, 1, 20, false, false, 0, false));
            state.setTickMs(100);

            Point head = state.getSnake().getHead();
            Point willHit = new Point(head.x + GameConfig.UNIT_SIZE, head.y);
            state.getObstacles().add(willHit);
            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE));

            state.update();

            assertFalse(state.isRunning());
            assertTrue(state.consumeEvents().stream().anyMatch(e -> e instanceof GameEvent.GameOver));
        }
    }

    @Test
    void movingObstacleCollisionBeforeMove_endsTickEarlyWithGameOverEvent() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            SettingsSnapshot ss = snapshot(GameMode.STANDARD, 1, 20, false, true, 0, false);
            GameState state = new GameState(0L, false, ss);
            state.setTickMs(100);

            Point head = new Point(state.getSnake().getHead());
            MovingObstacleSnapshot mos = new MovingObstacleSnapshot(List.of(head), 0, 0);
            Rectangle playArea = new Rectangle(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
            state.getMovingObstacles().add(MovingObstacle.fromSnapshot(mos, playArea, new java.util.Random(0)));

            state.update();
            assertFalse(state.isRunning());
            assertTrue(state.consumeEvents().stream().anyMatch(e -> e instanceof GameEvent.GameOver));
        }
    }

    @Test
    void raceMode_advancesMapAndEmitsEvents() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.withAutosaveSuppressed(() -> GameSettings.setCurrentMode(GameMode.RACE));

            GameState state = new GameState(123L, false, snapshot(GameMode.RACE, 1, 1, false, false, 0, false));
            state.setTickMs(3000); // shorten unlock message duration in ticks

            state.getApple().setPosition(new Point(state.getSnake().getHead()));
            state.update();

            List<GameEvent> events = state.consumeEvents();
            assertTrue(events.stream().anyMatch(e -> e instanceof GameEvent.AppleEaten));
            assertTrue(events.stream().anyMatch(e -> e instanceof GameEvent.MapAdvanced ma && ma.newMapId() == 2));
            assertEquals(2, state.getCurrentMapId());
            assertNotNull(state.getUnlockMessage());

            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE));
            state.update();
            assertNull(state.getUnlockMessage(), "Unlock message should expire quickly with tickMs=3000");
        }
    }

    @Test
    void movingObstaclesAutoIncrement_addsObstacleEveryConfiguredThreshold() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            SettingsSnapshot ss = snapshot(GameMode.STANDARD, 1, 20, false, true, 0, true);
            GameState state = new GameState(7L, false, ss);
            state.setTickMs(100);

            // Eat 10 apples; the increment happens on the 10th.
            for (int i = 0; i < GameConfig.MOVING_OBSTACLE_INCREMENT_APPLES; i++) {
                state.getApple().setPosition(new Point(state.getSnake().getHead()));
                state.update();
                state.consumeEvents();
            }
            assertEquals(1, state.getMovingObstacles().size());
        }
    }
}
