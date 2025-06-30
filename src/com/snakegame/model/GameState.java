package com.snakegame.model;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapConfig;
import com.snakegame.mode.MapManager;
import com.snakegame.sound.SoundPlayer;
import com.snakegame.util.ProgressManager;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameState {
    private final Snake snake;
    private final Apple apple;
    private int score = 0;
    private boolean running = true;

    private int applesEaten = 0;
    private boolean doubleScoreActive = false;
    private long doubleScoreEndTime = 0;

    private boolean slowed = false;
    private long slowEndTime = 0;

    private boolean reversedControls = false;
    private long reverseEndTime = 0;

    private final List<Point> obstacles = new ArrayList<>();

    public GameState() {
        // Initialize snake
        Point start = new Point(
                GameConfig.UNIT_SIZE * 5,
                GameConfig.UNIT_SIZE * 5
        );
        this.snake = new Snake(start, 6, Direction.RIGHT);

        // Configure obstacles based on selected mode
        GameMode mode = GameSettings.getCurrentMode();
        if (mode == GameMode.STANDARD) {
            if (GameSettings.isObstaclesEnabled()) {
                generateObstacles(15);
            }
        } else {
            int mapId = GameSettings.getSelectedMapId();
            MapConfig cfg = MapManager.getMap(mapId);
            if (cfg != null) {
                obstacles.addAll(cfg.getObstacles());
            }
            if (mode == GameMode.RACE) {
                // ensure first map is unlocked
                ProgressManager.unlockMap(mapId);
            }
        }

        // Spawn first apple avoiding obstacles and snake body
        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);
        this.apple = new Apple(forbidden);
    }

    private void generateObstacles(int count) {
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;
        Random rand = new Random();

        Set<Point> forbidden = new HashSet<>(snake.getBody());

        while (obstacles.size() < count) {
            Point p = new Point(
                    rand.nextInt(maxX) * GameConfig.UNIT_SIZE,
                    rand.nextInt(maxY) * GameConfig.UNIT_SIZE
            );
            if (!forbidden.contains(p) && !obstacles.contains(p)) {
                obstacles.add(p);
            }
        }
    }

    public void update() {
        if (!running) return;

        updateEffects();

        boolean ateApple = snake.getHead().equals(apple.getPosition());
        AppleType appleType = apple.getType();
        snake.move(ateApple);

        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);

        if (ateApple) {
            applesEaten++;
            SoundPlayer.play("eatApple.wav");

            int baseScore = appleType.getScoreValue();
            score += doubleScoreActive ? baseScore * 2 : baseScore;

            applyAppleEffect(appleType);

            // Race mode progression
            if (GameSettings.getCurrentMode() == GameMode.RACE
                    && applesEaten > 0
                    && applesEaten % GameSettings.getRaceThreshold() == 0) {

                int nextMap = GameSettings.getSelectedMapId() + 1;
                MapConfig nextCfg = MapManager.getMap(nextMap);
                if (nextCfg != null) {
                    GameSettings.setSelectedMapId(nextMap);
                    ProgressManager.unlockMap(nextMap);
                    obstacles.clear();
                    obstacles.addAll(nextCfg.getObstacles());
                }
            }

            // Spawn next apple
            apple.spawnRandomlyWeighted(applesEaten, score, forbidden);
        } else {
            long now = System.currentTimeMillis();
            if (apple.getVisibleDurationMs() > 0
                    && now - apple.getSpawnTime() > apple.getVisibleDurationMs()) {
                apple.spawnNew(AppleType.NORMAL, forbidden);
            }
        }

        checkCollision();
    }


    private void updateEffects() {
        long now = System.currentTimeMillis();

        if (doubleScoreActive && now >= doubleScoreEndTime) {
            doubleScoreActive = false;
        }

        if (slowed && now >= slowEndTime) {
            slowed = false;
        }

        if (reversedControls && now >= reverseEndTime) {
            reversedControls = false;
        }
    }

    private void applyAppleEffect(AppleType type) {
        long now = System.currentTimeMillis();
        switch (type) {
            case GOLDEN -> {
                doubleScoreActive = true;
                doubleScoreEndTime = now + GameConfig.GOLDEN_DURATION_MS;
            }
            case SLOWDOWN -> {
                slowed = true;
                slowEndTime = now + GameConfig.SLOWDOWN_DURATION_MS;
            }
            case REVERSE -> {
                reversedControls = true;
                reverseEndTime = now + GameConfig.REVERSE_DURATION_MS;
            }
        }
    }



    private void checkCollision() {
        Point head = snake.getHead();

        // Border collision
        if (head.x < 0 || head.x >= GameConfig.SCREEN_WIDTH || head.y < 0 || head.y >= GameConfig.SCREEN_HEIGHT) {
            running = false;
        }

        // Self collision
        if (snake.isSelfColliding()) {
            running = false;
        }

        // obstacle collision

        if (obstacles.contains(head)) {
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getScore() {
        return score;
    }

    public Snake getSnake() {
        return snake;
    }

    public Apple getApple() {
        return apple;
    }

    public void setDirection(Direction direction) {
        snake.setDirection(direction);
    }

    public boolean isDoubleScoreActive() {
        return doubleScoreActive;
    }

    public boolean isSlowed() {
        return slowed;
    }

    public long getDoubleScoreEndTime() {
        return doubleScoreEndTime;
    }

    public boolean isReversedControls() {
        return reversedControls;
    }

    public long getSlowEndTime() {
        return slowEndTime;
    }

    public List<Point> getObstacles() {
        return obstacles;
    }
}
