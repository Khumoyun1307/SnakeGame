package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.model.*;
import com.snakegame.sound.SoundPlayer;
import com.snakegame.util.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameController implements ActionListener, KeyListener {

    private final GameState gameState;
    private final Timer timer;
    private final Runnable repaintCallback;
    private final Runnable restartCallback;
    private final Runnable goToMainMenuCallback;
    private boolean paused = false;
    private boolean inputLocked = false;

    public GameController(GameState gameState, Runnable repaintCallback, Runnable restartCallback, Runnable goToMainMenuCallback) {
        this.gameState = gameState;
        this.repaintCallback = repaintCallback;
        this.restartCallback = restartCallback;
        this.goToMainMenuCallback = goToMainMenuCallback;
        this.timer = new Timer(GameSettings.getSpeedDelayFromDifficultyLevel(), this);
    }

    public void start() {
        timer.start();
    }

    private void pauseGame(Component parentComponent) {
        paused = true;
        timer.stop();

        int choice = showPauseMenu(parentComponent);

        switch (choice) {
            case 0 -> resumeGame();
            case 1 -> handleRestartFromPause();     // 🔁 Restart
            case 2 -> handleBackToMenuFromPause();  // 🏠 Back to Menu
            case 3 -> handleExitFromPause();        // ❌ Exit
        }
    }

    private void handleRestartFromPause() {
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Do you want to save your score before restarting?",
                "Save Score?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        switch (choice) {
            case JOptionPane.YES_OPTION -> {
                if (gameState.getScore() > 0) {
                    ScoreManager.addScore(gameState.getScore());
                }
                restartCallback.run();
            }
            case JOptionPane.NO_OPTION -> restartCallback.run();
            case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION -> {
                // Do nothing
            }
        }
    }


    private void handleBackToMenuFromPause() {
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Do you want to save your score before quitting?",
                "Save Score?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        switch (choice) {
            case JOptionPane.YES_OPTION -> {
                if (gameState.getScore() > 0) {
                    ScoreManager.addScore(gameState.getScore());
                }
                goToMainMenuCallback.run();
            }
            case JOptionPane.NO_OPTION -> goToMainMenuCallback.run();
            case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION -> {
                // Do nothing — stay in game
            }
        }
    }

    private void handleExitFromPause() {
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Do you want to save your score before exiting?",
                "Save Score?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        switch (choice) {
            case JOptionPane.YES_OPTION -> {
                if (gameState.getScore() > 0) {
                    ScoreManager.addScore(gameState.getScore());
                }
                System.exit(0);
            }
            case JOptionPane.NO_OPTION -> System.exit(0);
            case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION -> {
                // Do nothing
            }
        }
    }


    private void resumeGame() {
        paused = false;
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && gameState.isRunning()) {
            // Adjust timer delay based on slowed state
            int baseDelay = GameSettings.getSpeedDelayFromDifficultyLevel();
            if (gameState.isSlowed()) {
                timer.setDelay(baseDelay + GameConfig.SLOWDOWN_OFFSET_MS);
            } else {
                timer.setDelay(baseDelay);
            }

            gameState.update();
            inputLocked = false;
            repaintCallback.run();

            if (!gameState.isRunning()) {
                showGameOverMenu();
            }
        }
    }

    private int showPauseMenu(Component parentComponent) {
        String[] options = {"Resume", "Restart", "Back to Menu", "Exit"};
        return JOptionPane.showOptionDialog(
                parentComponent,
                "Game Paused",
                "Pause Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
    }


    private void showGameOverMenu() {
        timer.stop();
        SoundPlayer.play("game_over.wav");
        if (gameState.getScore() > 0) {
            ScoreManager.addScore(gameState.getScore());
        }

        String[] options = {"Restart", "Back to Menu", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Game Over",
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0 -> restartCallback.run();
            case 1 -> goToMainMenuCallback.run(); // ✅ NEW
            case 2 -> System.exit(0);
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            pauseGame((Component) e.getSource());
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
            inputLocked = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
