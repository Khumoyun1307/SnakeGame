package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.controller.input.AiDirectionProvider;
import com.snakegame.controller.input.DirectionProvider;
import com.snakegame.controller.input.PlayerDirectionProvider;
import com.snakegame.model.Direction;
import com.snakegame.model.GameState;
import com.snakegame.mode.GameMode;
import com.snakegame.ui.DialogService;
import com.snakegame.util.ProgressManager;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Wires together the simulation, input handling, and loop/flow coordination for a single run.
 *
 * <p>This controller listens to keyboard events, records player inputs for deterministic replays,
 * and delegates tick processing to {@link GameLoop} and {@link GameFlow}.</p>
 */
public class GameController implements KeyListener, LoopControl {

    private final GameState gameState;
    private final GameMode runMode;

    private boolean inputLocked = false;

    private final RunRecorder runRecorder;
    private final GameFlow gameFlow;
    private final GameLoop gameLoop;

    /**
     * Creates a new controller for the provided game state.
     *
     * @param gameState simulation state for this run
     * @param repaintCallback invoked after each tick to refresh rendering
     * @param restartCallback invoked when the user restarts the run
     * @param goToMainMenuCallback invoked when the run should return to the main menu
     * @param settingsCallback invoked when the user requests settings from in-game UI
     */
    public GameController(GameState gameState,
                          Runnable repaintCallback,
                          Runnable restartCallback,
                          Runnable goToMainMenuCallback,
                          Runnable settingsCallback) {
        this.gameState = gameState;

        SettingsSnapshot runSettings = gameState.getRunSettingsSnapshot();
        this.runMode = (runSettings != null) ? runSettings.currentMode() : GameSettings.getCurrentMode();
        int baseTickMs = (runSettings != null)
                ? GameSettings.speedDelayFromDifficultyLevel(runSettings.difficultyLevel())
                : GameSettings.getSpeedDelayFromDifficultyLevel();

        DirectionProvider directionProvider = (runMode == GameMode.AI)
                ? new AiDirectionProvider(GameSettings.getAiMode())
                : new PlayerDirectionProvider();

        // Mirror the old behavior: playing a map-based mode unlocks that map.
        if ((runMode == GameMode.MAP_SELECT || runMode == GameMode.RACE) && !GameSettings.isDeveloperModeEnabled()) {
            ProgressManager.unlockMap(gameState.getCurrentMapId());
        }

        this.runRecorder = new RunRecorder(runMode);
        DialogService dialogs = new DialogService();
        this.gameFlow = new GameFlow(
                gameState,
                runMode,
                runRecorder,
                restartCallback,
                goToMainMenuCallback,
                settingsCallback,
                dialogs,
                this
        );
        this.gameLoop = new GameLoop(
                gameState,
                baseTickMs,
                runMode,
                directionProvider,
                repaintCallback,
                () -> inputLocked = false,
                gameFlow
        );
    }

    /**
     * Starts the underlying timer-driven game loop.
     */
    public void start() {
        gameLoop.start();
    }

    /** {@inheritDoc} */
    @Override
    public void pause() {
        gameLoop.pause();
    }

    /** {@inheritDoc} */
    @Override
    public void resume() {
        gameLoop.resume();
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        gameLoop.stop();
    }

    /**
     * Handles key presses during gameplay.
     *
     * <p>Direction changes are rate-limited to at most one per simulation tick via
     * {@code inputLocked}. Reverse-controls power-ups invert arrow key mappings.</p>
     *
     * @param e key event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (runMode == GameMode.AI && e.getKeyCode() != KeyEvent.VK_SPACE) return;

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            gameFlow.onPauseRequested((Component) e.getSource());
            return;
        }

        if (inputLocked) return;

        boolean reversed = gameState.isReversedControls();
        Direction newDirection = switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> reversed ? Direction.DOWN : Direction.UP;
            case KeyEvent.VK_DOWN -> reversed ? Direction.UP : Direction.DOWN;
            case KeyEvent.VK_LEFT -> reversed ? Direction.RIGHT : Direction.LEFT;
            case KeyEvent.VK_RIGHT -> reversed ? Direction.LEFT : Direction.RIGHT;
            default -> null;
        };

        if (newDirection != null) {
            gameState.setDirection(newDirection);
            runRecorder.recordDirectionChange(gameState.getTick(), newDirection);
            inputLocked = true;
        }
    }

    /** {@inheritDoc} */
    @Override public void keyReleased(KeyEvent e) { }
    /** {@inheritDoc} */
    @Override public void keyTyped(KeyEvent e) { }
}
