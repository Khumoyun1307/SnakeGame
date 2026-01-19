package com.snakegame.view;

import com.snakegame.controller.GameController;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameSnapshot;
import com.snakegame.model.GameState;
import com.snakegame.util.ProgressManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private GameState gameState;
    private GameController controller;

    public GamePanel() {
        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        initGame();
    }

    public GamePanel(GameSnapshot snapshot) {
        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        initGame(snapshot);
    }

    private void initGame() { initGame(null); }

    private void initGame(GameSnapshot snapshot) {
        this.gameState = new GameState();
        if (snapshot != null) {
            this.gameState.restore(snapshot);
        }

        Runnable restartCallback = () -> SwingUtilities.invokeLater(() -> {
            removeKeyListener(controller);
            ProgressManager.clearSavedGame();
            initGame();
            repaint();
            requestFocusInWindow();
            controller.start();
        });

        Runnable goToMainMenuCallback = () -> SwingUtilities.invokeLater(() -> {
            removeKeyListener(controller);
            firePropertyChange("goToMenu", false, true);
        });

        Runnable settingsCallback = () -> this.firePropertyChange("showSettings", false, true);

        this.controller = new GameController(gameState, this::repaint, restartCallback, goToMainMenuCallback, settingsCallback);
        this.addKeyListener(controller);
    }

    public void startGame() {
        requestFocusInWindow();
        controller.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameRenderer.renderWorld(g, gameState, null); // draw at 0,0 in world coords
    }
}
