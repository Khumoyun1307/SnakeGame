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

public class GameController implements KeyListener, LoopControl {

    private final GameState gameState;
    private final GameMode runMode;

    private boolean inputLocked = false;

    private final RunRecorder runRecorder;
    private final GameFlow gameFlow;
    private final GameLoop gameLoop;

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
        if (runMode != GameMode.STANDARD) {
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

    public void start() {
        gameLoop.start();
    }

    @Override
    public void pause() {
        gameLoop.pause();
    }

    @Override
    public void resume() {
        gameLoop.resume();
    }

    @Override
    public void stop() {
        gameLoop.stop();
    }

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

    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
}
