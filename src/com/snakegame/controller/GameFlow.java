package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.model.GameEvent;
import com.snakegame.model.GameSnapshot;
import com.snakegame.model.GameState;
import com.snakegame.mode.GameMode;
import com.snakegame.sound.MusicManager;
import com.snakegame.sound.SoundPlayer;
import com.snakegame.ui.DialogService;
import com.snakegame.util.ProgressManager;
import com.snakegame.util.ScoreManager;

import java.awt.*;
import java.util.List;

public final class GameFlow implements TickHandler {

    private final GameState gameState;
    private final GameMode runMode;
    private final RunRecorder runRecorder;
    private final Runnable restartCallback;
    private final Runnable goToMainMenuCallback;
    private final Runnable settingsCallback;
    private final DialogService dialogs;
    private final LoopControl loopControl;

    public GameFlow(GameState gameState,
                    GameMode runMode,
                    RunRecorder runRecorder,
                    Runnable restartCallback,
                    Runnable goToMainMenuCallback,
                    Runnable settingsCallback,
                    DialogService dialogs,
                    LoopControl loopControl) {
        this.gameState = gameState;
        this.runMode = runMode;
        this.runRecorder = runRecorder;
        this.restartCallback = restartCallback;
        this.goToMainMenuCallback = goToMainMenuCallback;
        this.settingsCallback = settingsCallback;
        this.dialogs = dialogs;
        this.loopControl = loopControl;
    }

    public void onPauseRequested(Component parent) {
        loopControl.pause();

        dialogs.showPauseDialog(
                parent,
                gameState.getScore(),
                () -> loopControl.resume(),
                () -> dialogs.showInGameSettings(parent),
                () -> {
                    ProgressManager.saveGame(GameSnapshot.captureFrom(gameState));
                    goToMainMenuCallback.run();
                },
                () -> handleRestartFromPause(parent),
                () -> handleBackToMenuFromPause(parent),
                () -> handleExitFromPause(parent)
        );
    }

    @Override
    public void handleTickEvents(List<GameEvent> events) {
        for (GameEvent event : events) {
            if (event instanceof GameEvent.AppleEaten) {
                SoundPlayer.play("eatApple.wav");
            } else if (event instanceof GameEvent.MapAdvanced mapAdvanced && runMode == GameMode.RACE) {
                int newMapId = mapAdvanced.newMapId();
                ProgressManager.unlockMap(newMapId);
                GameSettings.setSelectedMapId(newMapId);
            }
        }
    }

    @Override
    public void onGameOver() {
        loopControl.stop();
        MusicManager.stop();
        SoundPlayer.play("game_over.wav");

        // Save replay BEFORE we mutate scores/settings
        runRecorder.saveReplay(gameState);

        ScoreManager.recordFinishedRun(gameState);
        ProgressManager.clearSavedGame();

        dialogs.showGameOverDialog(
                gameState.getScore(),
                restartCallback,
                settingsCallback,
                goToMainMenuCallback
        );
    }

    private void handleRestartFromPause(Component parent) {
        int choice = dialogs.confirmSaveScore(parent, "restarting");
        switch (choice) {
            case DialogService.YES_OPTION -> {
                ScoreManager.recordFinishedRun(gameState);
                restartCallback.run();
            }
            case DialogService.NO_OPTION -> restartCallback.run();
            default -> { }
        }
    }

    private void handleBackToMenuFromPause(Component parent) {
        int choice = dialogs.confirmSaveScore(parent, "quitting");
        switch (choice) {
            case DialogService.YES_OPTION -> {
                ScoreManager.recordFinishedRun(gameState);
                goToMainMenuCallback.run();
            }
            case DialogService.NO_OPTION -> goToMainMenuCallback.run();
            default -> { }
        }
    }

    private void handleExitFromPause(Component parent) {
        int saveChoice = dialogs.confirmSaveScore(parent, "exiting");
        if (saveChoice == DialogService.CANCEL_OPTION || saveChoice == DialogService.CLOSED_OPTION) return;

        if (saveChoice == DialogService.YES_OPTION) {
            ScoreManager.addScore(gameState.getScore());
        }

        if (dialogs.confirmExit(parent, "Are you sure you want to quit?")) {
            System.exit(0);
        }
    }
}
