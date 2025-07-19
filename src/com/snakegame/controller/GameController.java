package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.model.*;
import com.snakegame.sound.MusicManager;
import com.snakegame.sound.SoundPlayer;
import com.snakegame.ui.SettingsPanel;
import com.snakegame.util.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class GameController implements ActionListener, KeyListener {

    private final GameState gameState;
    private final Timer timer;
    private final Runnable repaintCallback;
    private final Runnable restartCallback;
    private final Runnable goToMainMenuCallback;
    private final Runnable settingsCallback;
    private boolean paused = false;
    private boolean inputLocked = false;

    public GameController(GameState gameState,
                          Runnable repaintCallback,
                          Runnable restartCallback,
                          Runnable goToMainMenuCallback,
                          Runnable settingsCallback) {
        this.gameState = gameState;
        this.repaintCallback = repaintCallback;
        this.restartCallback = restartCallback;
        this.goToMainMenuCallback = goToMainMenuCallback;
        this.settingsCallback = settingsCallback;
        this.timer = new Timer(GameSettings.getSpeedDelayFromDifficultyLevel(), this);
    }

    public void start() {
        timer.start();
    }

    private void pauseGame(Component parent) {
        paused = true;
        timer.stop();
        showPauseDialog(parent);
    }

    protected void showPauseDialog(Component parent) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Game Paused",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setBackground(Color.DARK_GRAY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel title = new JLabel("⏸ PAUSED");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Score: " + gameState.getScore());
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setFont(new Font("Consolas", Font.PLAIN, 18));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(20));

        // Helper to create each button
        Consumer<JButton> styleButton = btn -> {
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);
        };

        JButton resume = new JButton("▶ Resume");
        styleButton.accept(resume);
        resume.addActionListener(e -> {
            dialog.dispose();
            resumeGame();
        });

        JButton settings = new JButton("⚙ Settings");
        styleButton.accept(settings);
        settings.addActionListener(e -> {
            // dialog.dispose();
            showInGameSettings(parent);
        });

        JButton restart = new JButton("🔄 Restart");
        styleButton.accept(restart);
        restart.addActionListener(e -> {
            dialog.dispose();
            handleRestartFromPause();
        });

        JButton menu = new JButton("🏠 Main Menu");
        styleButton.accept(menu);
        menu.addActionListener(e -> {
            dialog.dispose();
            handleBackToMenuFromPause();
        });

        JButton exit = new JButton("❌ Exit");
        styleButton.accept(exit);
        exit.addActionListener(e -> {
            dialog.dispose();
            handleExitFromPause();
        });

        // Add buttons with spacing
        for (JButton b : new JButton[]{resume, settings, restart, menu, exit}) {
            panel.add(b);
            panel.add(Box.createVerticalStrut(10));
        }

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

     /**
     +     * Show SettingsPanel modally during a pause.
     +     * Back → re-open pause dialog.
     +     **/
    private void showInGameSettings(Component parent) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Settings",
                Dialog.ModalityType.APPLICATION_MODAL
                );
        // Pass Back = reopen the pause menu
        SettingsPanel panel = new SettingsPanel(e -> {
            dlg.dispose();
            // MusicManager.update(MusicManager.Screen.GAMEPLAY);
        });
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
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
        // 1) Save-score prompt
        int saveChoice = JOptionPane.showConfirmDialog(
            null,
            "Do you want to save your score before exiting?",
            "Save Score?",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (saveChoice == JOptionPane.CANCEL_OPTION || saveChoice == JOptionPane.CLOSED_OPTION) {
            return;
        }
        if (saveChoice == JOptionPane.YES_OPTION && gameState.getScore() > 0) {
            ScoreManager.addScore(gameState.getScore());
        }
        // 2) Confirm-exit prompt
        int exitConfirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to quit?",
                "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (exitConfirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }


    void resumeGame() {
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

    int showPauseMenu(Component parentComponent) {
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
        showGameOverDialog();
    }

    private void showGameOverMenuWithoutSound() {
        timer.stop();
        if (gameState.getScore() > 0) {
            ScoreManager.addScore(gameState.getScore());
        }
        showGameOverDialog();
    }

    protected void showGameOverDialog() {
        JDialog dialog = new JDialog(
                (Frame) null,
                "Game Over",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel over = new JLabel("💀 GAME OVER");
        over.setForeground(Color.RED);
        over.setFont(new Font("Ink Free", Font.BOLD, 28));
        over.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Final Score: " + gameState.getScore());
        scoreLabel.setForeground(Color.ORANGE);
        scoreLabel.setFont(new Font("Consolas", Font.PLAIN, 18));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(over);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(20));

        // Buttons: Restart, Settings, Main Menu, Exit
        JButton restart = new JButton("🔄 Restart");
        JButton settings = new JButton("⚙ Settings");
        JButton menu = new JButton("🏠 Main Menu");
        JButton exit = new JButton("❌ Exit");
        Consumer<JButton> styleButton = btn -> {
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);
        };
        for (JButton b : new JButton[]{restart, settings, menu, exit}) {
            styleButton.accept(b);
            panel.add(b);
            panel.add(Box.createVerticalStrut(10));
        }

        restart.addActionListener(e -> {
            dialog.dispose();
            restartCallback.run();
        });
        settings.addActionListener(e -> {
            dialog.dispose();
            settingsCallback.run();
        });
        menu.addActionListener(e -> {
            dialog.dispose();
            goToMainMenuCallback.run();
        });
        exit.addActionListener(e -> {
            dialog.dispose();
            int choice = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to quit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else {
                showGameOverMenuWithoutSound();
            }
        });

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
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
