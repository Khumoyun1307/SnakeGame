package com.snakegame.model;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
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

    // Effects (tick-based for determinism)
    private boolean doubleScoreActive = false;
    private long doubleScoreEndTick = 0;

    private boolean slowed = false;
    private long slowEndTick = 0;

    private boolean reversedControls = false;
    private long reverseEndTick = 0;

    private final List<Point> obstacles = new ArrayList<>();
    private final boolean watchOnly;

    // NEW: when watchOnly replay, use this snapshot instead of global GameSettings.
    private final SettingsSnapshot settingsOverride;

    // Unlock notification (tick-based)
    private String unlockMessage = null;
    private long unlockMessageEndTick = 0;
    private static final long UNLOCK_MSG_DURATION_MS = 3000;

    private final List<MovingObstacle> movingObstacles = new ArrayList<>();

    // Deterministic clock
    private int tickMs = 100; // set by controller at start
    private long tick = 0;    // increments each update()

    // Deterministic RNG
    private final Random rng;
    private final long seed;

    public GameState(long seed, boolean watchOnly) {
        this(seed, watchOnly, null);
    }

    // ✅ NEW: replay constructor should pass settingsSnapshot here
    public GameState(long seed, boolean watchOnly, SettingsSnapshot settingsOverride) {
        this.seed = seed;
        this.watchOnly = watchOnly;
        this.settingsOverride = settingsOverride;
        this.rng = new Random(seed);
        initGame();
    }

    public GameState(long seed) {
        this(seed, false, null);
    }

    /** Backwards compatible default: deterministic *per run* but seed is random. */
    public GameState() {
        this(System.nanoTime(), false, null);
    }

    // ---------- Settings accessors (snapshot-first for watchOnly) ----------

    private GameMode currentMode() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.currentMode()
                : GameSettings.getCurrentMode();
    }

    private int selectedMapId() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.selectedMapId()
                : GameSettings.getSelectedMapId();
    }

    private int raceThreshold() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.raceThreshold()
                : GameSettings.getRaceThreshold();
    }

    private boolean obstaclesEnabled() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.obstaclesEnabled()
                : GameSettings.isObstaclesEnabled();
    }

    private boolean movingObstaclesEnabled() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.movingObstaclesEnabled()
                : GameSettings.isMovingObstaclesEnabled();
    }

    private int movingObstacleCount() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.movingObstacleCount()
                : GameSettings.getMovingObstacleCount();
    }

    private boolean movingObstaclesAutoIncrement() {
        return (watchOnly && settingsOverride != null) ? settingsOverride.movingObstaclesAutoIncrement()
                : GameSettings.isMovingObstaclesAutoIncrement();
    }

    // ---------------------------------------------------------------------

    public long getSeed() { return seed; }
    Random rng() { return rng; } // package-private helper if needed later

    public void setTickMs(int tickMs) {
        this.tickMs = Math.max(1, tickMs);
        if (this.apple != null) {
            this.apple.setTickMs(this.tickMs);
        }
    }

    public long getTick() { return tick; }

    private long ticksFromMs(long ms) {
        return (ms + tickMs - 1L) / tickMs;
    }

    private void initGame() {
        obstacles.clear();
        movingObstacles.clear();

        GameMode mode = currentMode();

        if (mode == GameMode.STANDARD) {
            if (obstaclesEnabled()) {
                generateObstacles(15);
            }
            if (movingObstaclesEnabled()) {
                int count = movingObstacleCount();
                Rectangle playArea = new Rectangle(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
                for (int i = 0; i < count; i++) {
                    movingObstacles.add(createRandomMovingObstacle(playArea));
                }
            }
        } else {
            int mapId = selectedMapId();
            MapConfig cfg = MapManager.getMap(mapId);
            if (cfg != null) obstacles.addAll(cfg.getObstacles());
            if (!watchOnly) {
                ProgressManager.unlockMap(mapId);
            }
        }

        resetSnakeAndApple();
    }

    private MovingObstacle createRandomMovingObstacle(Rectangle playArea) {
        int lenRange = GameConfig.MAX_MOVING_OBSTACLE_LENGTH - GameConfig.MIN_MOVING_OBSTACLE_LENGTH + 1;
        int length = GameConfig.MIN_MOVING_OBSTACLE_LENGTH + rng.nextInt(lenRange);

        boolean vertical = rng.nextBoolean();

        int cellsX = playArea.width / GameConfig.UNIT_SIZE - (vertical ? 1 : length);
        int cellsY = playArea.height / GameConfig.UNIT_SIZE - (vertical ? length : 1);

        cellsX = Math.max(1, cellsX);
        cellsY = Math.max(1, cellsY);

        int startX = rng.nextInt(cellsX) * GameConfig.UNIT_SIZE;
        int startY = rng.nextInt(cellsY) * GameConfig.UNIT_SIZE;

        return new MovingObstacle(
                new Point(startX, startY),
                length,
                vertical,
                GameConfig.MOVING_OBSTACLE_SPEED,
                playArea,
                rng
        );
    }

    private void generateObstacles(int count) {
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;

        Set<Point> forbidden = new HashSet<>();
        if (snake != null) forbidden.addAll(snake.getBody());

        while (obstacles.size() < count) {
            Point p = new Point(
                    rng.nextInt(maxX) * GameConfig.UNIT_SIZE,
                    rng.nextInt(maxY) * GameConfig.UNIT_SIZE
            );
            if (!forbidden.contains(p) && !obstacles.contains(p)) obstacles.add(p);
        }
    }

    private void resetSnakeAndApple() {
        Point start = new Point(GameConfig.UNIT_SIZE * 5, GameConfig.UNIT_SIZE * 5);
        snake = new Snake(start, 6, Direction.RIGHT);

        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);
        if (movingObstaclesEnabled()) {
            movingObstacles.forEach(mo -> forbidden.addAll(mo.getSegments()));
        }

        apple = new Apple(forbidden, rng, this::getTick);
        apple.setTickMs(tickMs);

        applesEaten = 0;

        doubleScoreActive = false;
        slowed = false;
        reversedControls = false;

        doubleScoreEndTick = 0;
        slowEndTick = 0;
        reverseEndTick = 0;
    }

    public void update() {
        if (!running) return;

        tick++;

        updateEffects();
        updateNotifications();

        movingObstacles.forEach(MovingObstacle::update);
        if (!checkMovingObstacleCollision()) return;

        boolean ateApple = snake.getHead().equals(apple.getPosition());
        AppleType type = apple.getType();

        snake.move(ateApple);

        Point head = snake.getHead();
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;

        int cellX = head.x / GameConfig.UNIT_SIZE;
        int cellY = head.y / GameConfig.UNIT_SIZE;

        if (cellX < 0) cellX = maxX - 1;
        else if (cellX >= maxX) cellX = 0;

        if (cellY < 0) cellY = maxY - 1;
        else if (cellY >= maxY) cellY = 0;

        head.x = cellX * GameConfig.UNIT_SIZE;
        head.y = cellY * GameConfig.UNIT_SIZE;

        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);
        if (movingObstaclesEnabled()) {
            for (MovingObstacle mo : movingObstacles) forbidden.addAll(mo.getSegments());
        }

        if (ateApple) {
            applesEaten++;
            SoundPlayer.play("eatApple.wav");

            int baseScore = type.getScoreValue();
            score += doubleScoreActive ? baseScore * 2 : baseScore;

            applyAppleEffect(type);

            if (movingObstaclesEnabled()
                    && movingObstaclesAutoIncrement()
                    && movingObstacles.size() < GameConfig.MAX_MOVING_OBSTACLE_COUNT
                    && applesEaten % GameConfig.MOVING_OBSTACLE_INCREMENT_APPLES == 0) {

                Rectangle playArea = new Rectangle(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
                movingObstacles.add(createSafeMovingObstacle(playArea, snake.getHead()));
            }

            if (currentMode() == GameMode.RACE
                    && applesEaten >= raceThreshold()) {

                int nextMap = selectedMapId() + 1;
                MapConfig nextCfg = MapManager.getMap(nextMap);

                if (nextCfg != null) {
                    if (!watchOnly) {
                        GameSettings.setSelectedMapId(nextMap);
                        ProgressManager.unlockMap(nextMap);
                    }

                    obstacles.clear();
                    obstacles.addAll(nextCfg.getObstacles());
                    resetSnakeAndApple();
                    setUnlockMessage("Map " + nextMap + " unlocked!");
                }
            } else {
                apple.spawnRandomlyWeighted(applesEaten, score, forbidden);
            }
        } else {
            if (apple.isExpired()) {
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
        switch (type) {
            case GOLDEN -> {
                doubleScoreActive = true;
                doubleScoreEndTick = tick + ticksFromMs(GameConfig.GOLDEN_DURATION_MS);
            }
            case SLOWDOWN -> {
                slowed = true;
                slowEndTick = tick + ticksFromMs(GameConfig.SLOWDOWN_DURATION_MS);
            }
            case REVERSE -> {
                reversedControls = true;
                reverseEndTick = tick + ticksFromMs(GameConfig.REVERSE_DURATION_MS);
            }
            default -> { }
        }
    }

    private void updateEffects() {
        if (doubleScoreActive && tick >= doubleScoreEndTick) doubleScoreActive = false;
        if (slowed && tick >= slowEndTick) slowed = false;
        if (reversedControls && tick >= reverseEndTick) reversedControls = false;
    }

    private void updateNotifications() {
        if (unlockMessage != null && tick >= unlockMessageEndTick) unlockMessage = null;
    }

    private void setUnlockMessage(String message) {
        this.unlockMessage = message;
        this.unlockMessageEndTick = tick + ticksFromMs(UNLOCK_MSG_DURATION_MS);
    }

    private boolean headHitsMovingObstacle(Point head) {
        for (MovingObstacle mo : movingObstacles) {
            for (Point seg : mo.getSegments()) {
                if (seg.equals(head)) return true;
            }
        }
        return false;
    }

    private void checkCollision() {
        Point head = snake.getHead();

        if (snake.isSelfColliding()) running = false;
        if (obstacles.contains(head)) running = false;

        if (movingObstaclesEnabled()) {
            for (MovingObstacle mo : movingObstacles) {
                if (mo.getSegments().contains(head)) {
                    running = false;
                    return;
                }
            }
        }
    }

    private boolean checkMovingObstacleCollision() {
        if (movingObstaclesEnabled()) {
            if (headHitsMovingObstacle(snake.getHead())) {
                running = false;
                return false;
            }
        }
        return true;
    }

    // Getters
    public boolean isRunning() { return running; }
    public int getScore() { return score; }
    public Snake getSnake() { return snake; }
    public Apple getApple() { return apple; }
    public boolean isDoubleScoreActive() { return doubleScoreActive; }

    public long getDoubleScoreEndTime() { return doubleScoreEndTick; }
    public boolean isSlowed() { return slowed; }
    public long getSlowEndTime() { return slowEndTick; }
    public boolean isReversedControls() { return reversedControls; }
    public List<Point> getObstacles() { return obstacles; }
    public String getUnlockMessage() { return unlockMessage; }
    public List<MovingObstacle> getMovingObstacles() { return movingObstacles; }

    public void setDirection(Direction direction) { snake.setDirection(direction); }

    public long getReverseEndTime() { return reverseEndTick; }

    public int getApplesEaten() { return applesEaten; }
    public int getTickMs() { return tickMs; }

    public void restore(GameSnapshot snap) {
        GameSettings.setCurrentMode(snap.mode);
        GameSettings.setSelectedMapId(snap.selectedMapId);
        GameSettings.restore(snap.settingsSnapshot);

        this.score = snap.score;
        this.applesEaten = snap.applesEaten;

        this.snake = Snake.fromBody(snap.snakeBody, snap.direction);

        this.obstacles.clear();
        this.obstacles.addAll(snap.obstacles);

        this.movingObstacles.clear();
        Rectangle playArea = new Rectangle(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        for (MovingObstacleSnapshot mos : snap.movingObstacles) {
            this.movingObstacles.add(MovingObstacle.fromSnapshot(mos, playArea, rng));
        }

        Set<Point> forbidden = new HashSet<>(snake.getBody());
        forbidden.addAll(obstacles);

        this.apple = new Apple(forbidden, rng, this::getTick);
        this.apple.setTickMs(tickMs);
        this.apple.restore(snap.applePos, snap.appleType, snap.appleSpawnTime, snap.appleVisibleDurationMs);

        this.doubleScoreActive = snap.doubleScoreActive;
        this.doubleScoreEndTick = snap.doubleScoreEndTime;

        this.slowed = snap.slowed;
        this.slowEndTick = snap.slowEndTime;

        this.reversedControls = snap.reversedControls;
        this.reverseEndTick = snap.reverseEndTime;

        this.running = true;
        this.unlockMessage = null;
    }
}
