package com.snakegame.controller;

import com.snakegame.controller.input.DirectionProvider;
import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameEvent;
import com.snakegame.model.GameState;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.controller.GameLoop}.
 */
class GameLoopTest extends SnakeTestBase {

    @Test
    void actionPerformed_runsTickCallbacks_andPassesEventsToHandler() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L);
            int baseTickMs = 100;

            // Force an apple eat on first tick to get a deterministic event.
            state.getApple().setPosition(new Point(state.getSnake().getHead()));

            AtomicInteger repaints = new AtomicInteger(0);
            AtomicInteger afterTicks = new AtomicInteger(0);
            AtomicInteger handled = new AtomicInteger(0);

            TickHandler handler = new TickHandler() {
                @Override public void handleTickEvents(List<GameEvent> events) { handled.incrementAndGet(); }
                @Override public void onGameOver() { fail("not expected"); }
            };

            DirectionProvider provider = s -> Direction.UP;

            GameLoop loop = new GameLoop(
                    state,
                    baseTickMs,
                    GameMode.AI,
                    provider,
                    repaints::incrementAndGet,
                    afterTicks::incrementAndGet,
                    handler
            );

            loop.actionPerformed(new ActionEvent(this, 0, "tick"));

            assertEquals(1, repaints.get());
            assertEquals(1, afterTicks.get());
            assertEquals(1, handled.get());
            assertEquals(Direction.UP, state.getSnake().getDirection());
        }
    }

    @Test
    void actionPerformed_appliesSlowdownToTickMs() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L);
            int baseTickMs = 100;

            // Force slow mode without relying on random apple spawns.
            com.snakegame.testutil.Reflect.setField(state, "slowed", true);
            com.snakegame.testutil.Reflect.setField(state, "slowEndTick", Long.MAX_VALUE);
            state.getApple().setPosition(new Point(0, GameConfig.UNIT_SIZE));

            GameLoop loop = new GameLoop(
                    state,
                    baseTickMs,
                    GameMode.STANDARD,
                    s -> null,
                    () -> {},
                    () -> {},
                    new TickHandler() {
                        @Override public void handleTickEvents(List<GameEvent> events) { }
                        @Override public void onGameOver() { }
                    }
            );

            loop.actionPerformed(null);
            assertEquals(baseTickMs + GameConfig.SLOWDOWN_OFFSET_MS, state.getTickMs());
        }
    }
}
