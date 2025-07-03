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
    private Snake snake;
    private Apple apple;
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

    // Unlock notification
    private String unlockMessage = null;
    private long unlockMessageEndTime = 0;
    private static final long UNLOCK_MSG_DURATION_MS = 3000;

    private final List<MovingObstacle> movingObstacles = new ArrayList<>();
    private final Random rand = new Random();

    public GameState() {
        initGame();
    }

    private void initGame() {
        // Configure obstacles
        obstacles.clear();
        movingObstacles.clear();

        GameMode mode = GameSettings.getCurrentMode();
        if (mode == GameMode.STANDARD) {
            if (GameSettings.isObstaclesEnabled()) {
                generateObstacles(15);
            }
            if (GameSettings.isMovingObstaclesEnabled()) {
                int count = GameSettings.getMovingObstacleCount();
                Rectangle playArea = new Rectangle(0, 0,
                        GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
                for (int i = 0; i < count; i++) {
                    movingObstacles.add(createRandomMovingObstacle(playArea));
                }
            }

        } else {
            int mapId = GameSettings.getSelectedMapId();
            MapConfig cfg = MapManager.getMap(mapId);
            if (cfg != null) {
                obstacles.addAll(cfg.getObstacles());
            }
            // Ensure map unlocked for MAP_SELECT and RACE
            ProgressManager.unlockMap(mapId);
        }

        // Reset snake and apple for new map
        resetSnakeAndApple();
    }

    private MovingObstacle createRandomMovingObstacle(Rectangle playArea) {
        int lenRange = GameConfig.MAX_MOVING_OBSTACLE_LENGTH - GameConfig.MIN_MOVING_OBSTACLE_LENGTH + 1;
        int length = GameConfig.MIN_MOVING_OBSTACLE_LENGTH + rand.nextInt(lenRange);
        boolean vertical = rand.nextBoolean();
        int cellsX = playArea.width / GameConfig.UNIT_SIZE - (vertical ? 1 : length);
        int cellsY = playArea.height / GameConfig.UNIT_SIZE - (vertical ? length : 1);
        int startX = rand.nextInt(cellsX) * GameConfig.UNIT_SIZE;
        int startY = rand.nextInt(cellsY) * GameConfig.UNIT_SIZE;
        return new MovingObstacle(
                new Point(startX, startY), length, vertical,
                GameConfig.MOVING_OBSTACLE_SPEED, playArea
        );
    }

    private void generateObstacles(int count) {
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;
        Random rand = new Random();

        Set<Point> forbidden = new HashSet<>();
        if (snake != null) {
            forbidden.addAll(snake.getBody());
        }

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

    private void resetSnakeAndApple() {
        // Reset snake to default start
        Point start = new Point(
                GameConfig.UNIT_SIZE * 5,
                GameConfig.UNIT_SIZE * 5
        );
        snake = new Snake(start, 6, Direction.RIGHT);

        // Spawn apple avoiding obstacles and snake body
        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);
        if (GameSettings.isMovingObstaclesEnabled()) {
            movingObstacles.forEach(mo -> forbidden.addAll(mo.getSegments()));
        }
        apple = new Apple(forbidden);

        // Reset effects and apple count for new map
        applesEaten = 0;
        doubleScoreActive = false;
        slowed = false;
        reversedControls = false;
    }

    public void update() {
        if (!running) return;

        updateEffects();
        updateNotifications();

        if (GameSettings.isMovingObstaclesEnabled()) {
            movingObstacles.forEach(MovingObstacle::update);
        }

        boolean ateApple = snake.getHead().equals(apple.getPosition());
        AppleType type = apple.getType();
        snake.move(ateApple);

        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);

        if (ateApple) {
            applesEaten++;
            SoundPlayer.play("eatApple.wav");

            int baseScore = type.getScoreValue();
            score += doubleScoreActive ? baseScore * 2 : baseScore;

            applyAppleEffect(type);

            // === AUTO-INCREMENT LOGIC ===
            if (GameSettings.isMovingObstaclesEnabled()
                    && GameSettings.isMovingObstaclesAutoIncrement()
                    && movingObstacles.size() < GameConfig.MAX_MOVING_OBSTACLE_COUNT
                    && applesEaten % GameConfig.MOVING_OBSTACLE_INCREMENT_APPLES == 0) {

                // spawn one new obstacle, safely away from the snake head
                Rectangle playArea = new Rectangle(0, 0,
                        GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
                movingObstacles.add(createSafeMovingObstacle(playArea, snake.getHead()));
            }
            // === END AUTO-INCREMENT ===

            // Race mode transition
            if (GameSettings.getCurrentMode() == GameMode.RACE
                    && applesEaten >= GameSettings.getRaceThreshold()) {
                int nextMap = GameSettings.getSelectedMapId() + 1;
                MapConfig nextCfg = MapManager.getMap(nextMap);
                if (nextCfg != null) {
                    GameSettings.setSelectedMapId(nextMap);
                    ProgressManager.unlockMap(nextMap);
                    obstacles.clear();
                    obstacles.addAll(nextCfg.getObstacles());
                    resetSnakeAndApple();
                    setUnlockMessage("Map " + nextMap + " unlocked!");
                }
            } else {
                // Normal apple spawn
                apple.spawnRandomlyWeighted(applesEaten, score, forbidden);
            }
        } else {
            long now = System.currentTimeMillis();
            if (apple.getVisibleDurationMs() > 0
                    && now - apple.getSpawnTime() > apple.getVisibleDurationMs()) {
                apple.spawnNew(AppleType.NORMAL, forbidden);
            }
        }

        checkCollision();
    }

    private MovingObstacle createSafeMovingObstacle(Rectangle playArea, Point head) {
        MovingObstacle mo;
        do {
            mo = createRandomMovingObstacle(playArea);
        } while (mo.getSegments().stream().anyMatch(seg ->
                Math.abs(seg.x - head.x) < GameConfig.UNIT_SIZE * 5
                        && Math.abs(seg.y - head.y) < GameConfig.UNIT_SIZE * 5
        ));
        return mo;
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

    private void updateEffects() {
        long now = System.currentTimeMillis();
        if (doubleScoreActive && now >= doubleScoreEndTime) doubleScoreActive = false;
        if (slowed && now >= slowEndTime) slowed = false;
        if (reversedControls && now >= reverseEndTime) reversedControls = false;
    }

    private void updateNotifications() {
        if (unlockMessage != null && System.currentTimeMillis() >= unlockMessageEndTime) {
            unlockMessage = null;
        }
    }

    private void setUnlockMessage(String message) {
        this.unlockMessage = message;
        this.unlockMessageEndTime = System.currentTimeMillis() + UNLOCK_MSG_DURATION_MS;
    }

    private void checkCollision() {
        Point head = snake.getHead();
        // Border collision
        if (head.x < 0 || head.x >= GameConfig.SCREEN_WIDTH
                || head.y < 0 || head.y >= GameConfig.SCREEN_HEIGHT) {
            running = false;
        }
        // Self collision
        if (snake.isSelfColliding()) running = false;
        // Obstacle collision
        if (obstacles.contains(head)) running = false;

        if (GameSettings.isMovingObstaclesEnabled()) {
            for (MovingObstacle mo : movingObstacles) {
                if (mo.getSegments().contains(head)) {
                    running = false;
                    return;
                }
            }
        }
    }

    // Getters
    public boolean isRunning() { return running; }
    public int getScore() { return score; }
    public Snake getSnake() { return snake; }
    public Apple getApple() { return apple; }
    public boolean isDoubleScoreActive() { return doubleScoreActive; }
    public long getDoubleScoreEndTime() { return doubleScoreEndTime; }
    public boolean isSlowed() { return slowed; }
    public long getSlowEndTime() { return slowEndTime; }
    public boolean isReversedControls() { return reversedControls; }
    public List<Point> getObstacles() { return obstacles; }
    /** Returns current unlock notification or null */
    public String getUnlockMessage() { return unlockMessage; }
    public List<MovingObstacle> getMovingObstacles() {
        return movingObstacles;
    }
    public void setDirection(Direction direction) {
        snake.setDirection(direction);
    }

}
