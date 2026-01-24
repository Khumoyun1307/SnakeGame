package com.snakegame.view;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.model.*;
import com.snakegame.util.ScoreManager;

import java.awt.*;

/**
 * Rendering utility for drawing the game world to a Swing {@link Graphics} context.
 *
 * <p>Supports both live gameplay (using global {@link GameSettings}) and watch-only replay
 * rendering (using a provided {@link SettingsSnapshot} without mutating global settings).</p>
 */
public final class GameRenderer {
    private GameRenderer() {}

    // ---------------------- LIVE GAME ----------------------
    // Live gameplay can keep using global GameSettings.
    /**
     * Renders the world scaled to fit the given panel bounds (live gameplay).
     *
     * @param g graphics context
     * @param gameState current game state
     * @param panelW panel width in pixels
     * @param panelH panel height in pixels
     */
    public static void renderScaleToFit(Graphics g, GameState gameState, int panelW, int panelH) {
        renderScaleToFit(g, gameState, panelW, panelH, null);
    }

    /**
     * Renders the world centered at a fixed size within the given panel bounds (live gameplay).
     *
     * @param g graphics context
     * @param gameState current game state
     * @param panelW panel width in pixels
     * @param panelH panel height in pixels
     */
    public static void renderFixed(Graphics g, GameState gameState, int panelW, int panelH) {
        renderFixed(g, gameState, panelW, panelH, null);
    }

    // ---------------------- WATCH-ONLY (REPLAY) ----------------------
    // Replay should call these overloads with SettingsSnapshot from the replay data.
    /**
     * Renders the world scaled to fit the given panel bounds using a provided settings snapshot.
     *
     * @param g graphics context
     * @param gameState replay game state
     * @param panelW panel width in pixels
     * @param panelH panel height in pixels
     * @param settings frozen settings to use for rendering (may be {@code null})
     */
    public static void renderScaleToFit(Graphics g, GameState gameState, int panelW, int panelH, SettingsSnapshot settings) {
        if (gameState == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int worldW = GameConfig.SCREEN_WIDTH;
            int worldH = GameConfig.SCREEN_HEIGHT;

            double sx = panelW / (double) worldW;
            double sy = panelH / (double) worldH;
            double s = Math.min(sx, sy);

            int drawW = (int) Math.round(worldW * s);
            int drawH = (int) Math.round(worldH * s);

            int offX = (panelW - drawW) / 2;
            int offY = (panelH - drawH) / 2;

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, panelW, panelH);

            g2.translate(offX, offY);
            g2.scale(s, s);

            renderWorld(g2, gameState, settings);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Renders the world centered at a fixed size using a provided settings snapshot.
     *
     * @param g graphics context
     * @param gameState replay game state
     * @param panelW panel width in pixels
     * @param panelH panel height in pixels
     * @param settings frozen settings to use for rendering (may be {@code null})
     */
    public static void renderFixed(Graphics g, GameState gameState, int panelW, int panelH, SettingsSnapshot settings) {
        if (gameState == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int worldW = GameConfig.SCREEN_WIDTH;
            int worldH = GameConfig.SCREEN_HEIGHT;

            int offX = (panelW - worldW) / 2;
            int offY = (panelH - worldH) / 2;

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, panelW, panelH);

            g2.translate(offX, offY);

            renderWorld(g2, gameState, settings);
        } finally {
            g2.dispose();
        }
    }

    // ------------------- INTERNAL -------------------

    /**
     * Renders the world in simulation coordinates (0,0 at the top-left of the playfield).
     *
     * @param g graphics context (already translated/scaled as desired)
     * @param gameState state to render
     * @param settings optional settings snapshot for watch-only rendering
     */
    static void renderWorld(Graphics g, GameState gameState, SettingsSnapshot settings) {
        boolean showGrid = (settings != null) ? settings.showGrid() : GameSettings.isShowGrid();
        if (showGrid) drawGrid(g);

        drawGame(g, gameState, settings);
    }

    private static void drawGrid(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE; i++) {
            g.drawLine(i * GameConfig.UNIT_SIZE, 0, i * GameConfig.UNIT_SIZE, GameConfig.SCREEN_HEIGHT);
            g.drawLine(0, i * GameConfig.UNIT_SIZE, GameConfig.SCREEN_WIDTH, i * GameConfig.UNIT_SIZE);
        }
    }

    private static void drawGame(Graphics g, GameState gameState, SettingsSnapshot settings) {
        GameSettings.Theme theme = (settings != null) ? settings.selectedTheme() : GameSettings.getSelectedTheme();

        Color snakeHeadColor, snakeBodyColor, obstacleColor;
        switch (theme) {
            case NEON -> {
                snakeHeadColor = Color.MAGENTA;
                snakeBodyColor = Color.CYAN;
                obstacleColor = Color.PINK;
            }
            case PIXEL_ART -> {
                snakeHeadColor = new Color(0, 255, 255);
                snakeBodyColor = new Color(0, 128, 128);
                obstacleColor  = new Color(128, 128, 0);
            }
            default -> {
                snakeHeadColor = Color.GREEN;
                snakeBodyColor = new Color(45, 180, 0);
                obstacleColor = Color.GRAY;
            }
        }

        String unlockMsg = gameState.getUnlockMessage();
        if (unlockMsg != null) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            int x = (GameConfig.SCREEN_WIDTH - fm.stringWidth(unlockMsg)) / 2;
            g.drawString(unlockMsg, x, 40);
        }

        if (!gameState.isRunning()) {
            drawGameOver(g);
            return;
        }

        Apple appleObj = gameState.getApple();
        AppleType type = appleObj.getType();
        Point pos = appleObj.getPosition();

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
                g.drawOval(drawX, drawY, size, size);
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
            g.setColor(obstacleColor);
            g.fillRect(obs.x, obs.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
            g.setColor(obstacleColor.darker());
            g.drawRect(obs.x, obs.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
        }

        boolean movingEnabled = (settings != null) ? settings.movingObstaclesEnabled() : GameSettings.isMovingObstaclesEnabled();
        if (movingEnabled) {
            Color moColor = new Color(obstacleColor.getRed(), obstacleColor.getGreen(), obstacleColor.getBlue(), 180);
            g.setColor(moColor);
            for (MovingObstacle mo : gameState.getMovingObstacles()) {
                for (Point p : mo.getSegments()) {
                    g.fillRect(p.x, p.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
                }
            }
        }

        int index = 0;
        for (Point p : gameState.getSnake().getBody()) {
            g.setColor(index++ == 0 ? snakeHeadColor : snakeBodyColor);
            g.fillOval(p.x, p.y, GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE);
        }

        if (gameState.isReversedControls()) {
            drawCenteredText(g, "🔄 Reverse Controls Active!", 60, Color.MAGENTA);
        } else if (gameState.isDoubleScoreActive()) {
            long secLeft = secondsLeft(gameState, gameState.getDoubleScoreEndTime());
            drawCenteredText(g, "💰 Double Points: " + secLeft + "s", 60, Color.YELLOW);
        } else if (gameState.isSlowed()) {
            long secLeft = secondsLeft(gameState, gameState.getSlowEndTime());
            drawCenteredText(g, "⏳ Slow Mode: " + secLeft + "s", 60, Color.CYAN);
        }

        drawScore(g, gameState);
    }

    private static long secondsLeft(GameState state, long endTick) {
        long ticksLeft = endTick - state.getTick();
        if (ticksLeft < 0) ticksLeft = 0;
        long msLeft = ticksLeft * (long) state.getTickMs();
        return msLeft / 1000L;
    }

    private static void drawCenteredText(Graphics g, String text, int y, Color color) {
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int x = (GameConfig.SCREEN_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private static void drawScore(Graphics g, GameState gameState) {
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

    private static void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        String gameOverText = "Game Over";
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(gameOverText,
                (GameConfig.SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2,
                GameConfig.SCREEN_HEIGHT / 2);
    }
}
