package com.snakegame.view;

import com.snakegame.controller.GameController;
import com.snakegame.model.*;
import com.snakegame.util.ScoreManager;

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


    private void initGame() {
        this.gameState = new GameState();

        // Restart callback to reset everything

        Runnable restartCallback = () -> {
            SwingUtilities.invokeLater(() -> {
                removeKeyListener(controller); // Remove old listener
                initGame();                   // Reinitialize
                repaint();
                requestFocusInWindow();
                controller.start();
            });
        };

        Runnable goToMainMenuCallback = () -> {
            SwingUtilities.invokeLater(() -> {
                removeKeyListener(controller);
                // Tell GameFrame to go back to main menu
                firePropertyChange("goToMenu", false, true);
            });
        };

        this.controller = new GameController(gameState, this::repaint, restartCallback, goToMainMenuCallback);
        this.addKeyListener(controller);
    }

    public void startGame() {
        requestFocusInWindow(); // ensure key input works
        controller.start();     // ✅ only start when called explicitly
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawGame(g);
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE; i++) {
            g.drawLine(i * GameConfig.UNIT_SIZE, 0, i * GameConfig.UNIT_SIZE, GameConfig.SCREEN_HEIGHT);
            g.drawLine(0, i * GameConfig.UNIT_SIZE, GameConfig.SCREEN_WIDTH, i * GameConfig.UNIT_SIZE);
        }
    }

    private void drawCenteredText(Graphics g, String text, int y, Color color) {
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int x = (GameConfig.SCREEN_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }


    private void drawGame(Graphics g) {
        if (!gameState.isRunning()) {
            drawGameOver(g);
            return;
        }

        // Draw apple

        Apple appleObj = gameState.getApple();
        AppleType type = appleObj.getType();
        Point pos = appleObj.getPosition();

        // Pumping effect for featured apples
        long now = System.currentTimeMillis();
        long sinceSpawn = now - appleObj.getSpawnTime();
        long duration = appleObj.getVisibleDurationMs();
        boolean animatePulse = duration > 0;

        float scale = 1.0f;
        if (animatePulse) {
            float pulse = (float) Math.sin((sinceSpawn % 1000) / 1000.0 * 2 * Math.PI);
            scale = 1.0f + 0.2f * pulse;
        }

        int size = (int) (GameConfig.UNIT_SIZE * scale);
        int offset = (GameConfig.UNIT_SIZE - size) / 2;
        int drawX = pos.x + offset;
        int drawY = pos.y + offset;

        g.setColor(type.getColor());
        switch (type) {
            case GOLDEN -> {
                g.fillOval(drawX, drawY, size, size);
                g.setColor(Color.WHITE);
                g.drawOval(drawX, drawY, size, size); // glow ring
            }
            case SLOWDOWN -> g.fillRect(drawX, drawY, size, size);
            case REVERSE -> {
                g.fillOval(drawX, drawY, size, size);
                g.setColor(Color.BLACK);
                g.drawLine(drawX + 4, drawY + 4, drawX + size - 4, drawY + size - 4);
                g.drawLine(drawX + size - 4, drawY + 4, drawX + 4, drawY + size - 4);
            }
            default -> g.fillOval(drawX, drawY, size, size);
        }

        for (Point obs : gameState.getObstacles()) {
            g.setColor(Color.GRAY);
            g.fillRect(obs.x, obs.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(obs.x, obs.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
        }

        // Draw snake
        int index = 0;
        for (Point p : gameState.getSnake().getBody()) {
            if (index++ == 0) {
                g.setColor(Color.GREEN); // head
            } else {
                g.setColor(new Color(45, 180, 0)); // body
            }
            g.fillOval(p.x, p.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
        }

        if (gameState.isReversedControls()) {
            drawCenteredText(g, "🔄 Reverse Controls Active!", 40, Color.MAGENTA);
        } else if (gameState.isDoubleScoreActive()) {
            long timeLeft = (gameState.getDoubleScoreEndTime() - System.currentTimeMillis()) / 1000;
            drawCenteredText(g, "💰 Double Points: " + timeLeft + "s", 40, Color.YELLOW);
        } else if (gameState.isSlowed()) {
            long timeLeft = (gameState.getSlowEndTime() - System.currentTimeMillis()) / 1000;
            drawCenteredText(g, "⏳ Slow Mode: " + timeLeft + "s", 40, Color.CYAN);
        }

        drawScore(g);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 25));
        String scoreText = "Score: " + gameState.getScore();
        String highScoreText = "High Score: " + ScoreManager.getHighScore();
        FontMetrics metrics = g.getFontMetrics();

        g.drawString(scoreText, 10, g.getFont().getSize());
        g.drawString(highScoreText,
                GameConfig.SCREEN_WIDTH - metrics.stringWidth(highScoreText) - 10,
                g.getFont().getSize());
    }


    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        String gameOverText = "Game Over";
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(gameOverText,
                (GameConfig.SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2,
                GameConfig.SCREEN_HEIGHT / 2);
    }

}
