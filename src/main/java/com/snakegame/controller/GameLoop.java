package com.snakegame.controller;

import com.snakegame.controller.input.DirectionProvider;
import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameEvent;
import com.snakegame.model.GameState;
import com.snakegame.mode.GameMode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Swing {@link Timer}-driven game loop that advances the simulation on a fixed tick cadence.
 *
 * <p>Each timer event computes an effective tick delay (accounting for slowdown effects), applies
 * AI input if enabled, advances the simulation by one tick, emits domain events, and triggers
 * repaint callbacks.</p>
 */
public final class GameLoop implements ActionListener {

    private final GameState gameState;
    private final Timer timer;
    private final Runnable repaintCallback;
    private final Runnable afterTickCallback;

    private final int baseTickMs;
    private final GameMode runMode;
    private final DirectionProvider directionProvider;
    private final TickHandler tickHandler;

    private boolean paused = false;

    /**
     * Creates a new loop for the provided run.
     *
     * @param gameState simulation state to advance each tick
     * @param baseTickMs base tick duration in milliseconds (clamped to at least 1)
     * @param runMode mode selected for the run
     * @param directionProvider input provider (AI or player)
     * @param repaintCallback invoked after each tick to repaint the UI
     * @param afterTickCallback invoked after {@link GameState#update()} (e.g., to unlock input)
     * @param tickHandler handles post-tick events and game-over transitions
     */
    public GameLoop(GameState gameState,
                    int baseTickMs,
                    GameMode runMode,
                    DirectionProvider directionProvider,
                    Runnable repaintCallback,
                    Runnable afterTickCallback,
                    TickHandler tickHandler) {
        this.gameState = gameState;
        this.baseTickMs = Math.max(1, baseTickMs);
        this.runMode = runMode;
        this.directionProvider = directionProvider;
        this.repaintCallback = repaintCallback;
        this.afterTickCallback = afterTickCallback;
        this.tickHandler = tickHandler;

        this.timer = new Timer(this.baseTickMs, this);
        this.gameState.setTickMs(this.baseTickMs);
    }

    /**
     * Starts the timer loop.
     */
    public void start() { timer.start(); }
    /**
     * Stops the timer loop.
     */
    public void stop() { timer.stop(); }

    /**
     * Pauses the loop (no further ticks are processed until resumed).
     */
    public void pause() {
        paused = true;
        timer.stop();
    }

    /**
     * Resumes the loop after a pause.
     */
    public void resume() {
        paused = false;
        timer.start();
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused || !gameState.isRunning()) return;

        int effectiveDelay = gameState.isSlowed()
                ? baseTickMs + GameConfig.SLOWDOWN_OFFSET_MS
                : baseTickMs;

        timer.setDelay(effectiveDelay);
        gameState.setTickMs(effectiveDelay);

        if (runMode == GameMode.AI) {
            Direction aiDir = directionProvider.nextDirection(gameState);
            if (aiDir != null) gameState.setDirection(aiDir);
        }

        gameState.update();
        afterTickCallback.run();

        List<GameEvent> events = gameState.consumeEvents();
        tickHandler.handleTickEvents(events);

        repaintCallback.run();

        boolean gameOverThisTick = false;
        for (GameEvent event : events) {
            if (event instanceof GameEvent.GameOver) {
                gameOverThisTick = true;
                break;
            }
        }

        if (gameOverThisTick) {
            stop();
            tickHandler.onGameOver();
        }
    }
}
