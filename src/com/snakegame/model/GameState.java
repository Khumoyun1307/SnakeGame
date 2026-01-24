package com.snakegame.model;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapConfig;
import com.snakegame.mode.MapManager;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Deterministic core simulation for a single Snake run.
 *
 * <p>The simulation advances in discrete ticks via {@link #update()}, emitting {@link GameEvent}s
 * for side effects such as sound, persistence, and UI transitions. To keep replays stable,
 * time-based effects are tracked in ticks rather than wall-clock time, and a seeded {@link Random}
 * drives all randomness.</p>
 */
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

    /**
     * Snapshot of gameplay-relevant settings captured at the start of a run.
     * For watch-only replays, this comes from the replay file.
     */
    private SettingsSnapshot runSettingsSnapshot;

    /** Current map id for MAP_SELECT/RACE (must advance even in watch-only replays). */
    private int currentMapId;

    private final List<GameEvent> pendingEvents = new ArrayList<>();

    // Unlock notification (tick-based)
    private String unlockMessage = null;
    private long unlockMessageEndTick = 0;
    private static final long UNLOCK_MSG_DURATION_MS = 3000;

    private final List<MovingObstacle> movingObstacles = new ArrayList<>();

    // Deterministic clock
    private int tickMs = 100; // set by controller at start
    private long tick = 0;    // increments each update()
    private long elapsedSimTimeMs = 0;

    // Deterministic RNG
    private final Random rng;
    private final long seed;

    /**
     * Creates a game state with the provided seed and playback mode.
     *
     * @param seed seed for deterministic RNG
     * @param watchOnly whether this instance is used for watch-only replay
     */
    public GameState(long seed, boolean watchOnly) {
        this(seed, watchOnly, null);
    }

    /**
     * Creates a game state configured with a frozen settings snapshot.
     *
     * @param seed seed for deterministic RNG
     * @param watchOnly whether this instance is used for watch-only replay
     * @param runSettingsSnapshot run settings snapshot (defaults to {@link GameSettings#snapshot()} when {@code null})
     */
    public GameState(long seed, boolean watchOnly, SettingsSnapshot runSettingsSnapshot) {
        this.seed = seed;
        this.watchOnly = watchOnly;
        this.runSettingsSnapshot = (runSettingsSnapshot != null) ? runSettingsSnapshot : GameSettings.snapshot();
        this.currentMapId = this.runSettingsSnapshot.selectedMapId();
        this.rng = new Random(seed);
        initGame();
    }

    /**
     * Creates a playable run with a fixed seed.
     *
     * @param seed seed for deterministic RNG
     */
    public GameState(long seed) {
        this(seed, false, null);
    }

    /** Backwards compatible default: deterministic *per run* but seed is random. */
    /**
     * Creates a new run seeded from {@link System#nanoTime()}.
     */
    public GameState() {
        this(System.nanoTime(), false, null);
    }

    // ---------- Settings accessors (frozen at run start) ----------

    private GameMode currentMode() {
        return runSettingsSnapshot != null ? runSettingsSnapshot.currentMode() : GameSettings.getCurrentMode();
    }

    private int selectedMapId() {
        return currentMapId;
    }

    private int raceThreshold() {
        return runSettingsSnapshot != null ? runSettingsSnapshot.raceThreshold() : GameSettings.getRaceThreshold();
    }

    private boolean obstaclesEnabled() {
        return runSettingsSnapshot != null ? runSettingsSnapshot.obstaclesEnabled() : GameSettings.isObstaclesEnabled();
    }

    private boolean movingObstaclesEnabled() {
        return runSettingsSnapshot != null ? runSettingsSnapshot.movingObstaclesEnabled() : GameSettings.isMovingObstaclesEnabled();
    }

    private int movingObstacleCount() {
        return runSettingsSnapshot != null ? runSettingsSnapshot.movingObstacleCount() : GameSettings.getMovingObstacleCount();
    }

    private boolean movingObstaclesAutoIncrement() {
        return runSettingsSnapshot != null ? runSettingsSnapshot.movingObstaclesAutoIncrement() : GameSettings.isMovingObstaclesAutoIncrement();
    }

    // ---------------------------------------------------------------------

    /**
     * Returns the RNG seed used for this run.
     *
     * @return seed
     */
    public long getSeed() { return seed; }
    Random rng() { return rng; } // package-private helper if needed later

    /**
     * Sets the duration (in milliseconds) represented by a single simulation tick.
     *
     * @param tickMs tick duration in milliseconds (values <= 0 are clamped to 1)
     */
    public void setTickMs(int tickMs) {
        this.tickMs = Math.max(1, tickMs);
        if (this.apple != null) {
            this.apple.setTickMs(this.tickMs);
        }
    }

    /**
     * Returns the current simulation tick.
     *
     * @return tick counter
     */
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

    /**
     * Advances the simulation by one tick.
     *
     * <p>This method updates effects, moves obstacles and the snake, applies wrap-around, handles
     * apple consumption/spawning, checks collisions, and records any {@link GameEvent}s emitted for
     * the tick. Call {@link #consumeEvents()} after updating to retrieve and clear emitted events.</p>
     */
    public void update() {
        if (!running) return;

        pendingEvents.clear();

        tick++;
        elapsedSimTimeMs += tickMs;

        updateEffects();
        updateNotifications();

        movingObstacles.forEach(MovingObstacle::update);
        if (!checkMovingObstacleCollision()) {
            pendingEvents.add(new GameEvent.GameOver(score));
            return;
        }

        boolean ateApple = snake.getHead().equals(apple.getPosition());
        AppleType type = apple.getType();

        snake.move(ateApple);

        Point head = snake.getHead();
        // Wrap head position around screen edges (toroidal playfield).
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
            pendingEvents.add(new GameEvent.AppleEaten(type));

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
                    // Advance map id even in watch-only replay.
                    currentMapId = nextMap;
                    pendingEvents.add(new GameEvent.MapAdvanced(nextMap));

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
        if (!running) {
            pendingEvents.add(new GameEvent.GameOver(score));
        }
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
    /**
     * Returns whether the simulation is still running (not game over).
     *
     * @return {@code true} if running
     */
    public boolean isRunning() { return running; }
    /**
     * Returns the current score.
     *
     * @return score
     */
    public int getScore() { return score; }
    /**
     * Returns the snake instance.
     *
     * @return snake
     */
    public Snake getSnake() { return snake; }
    /**
     * Returns the current apple.
     *
     * @return apple
     */
    public Apple getApple() { return apple; }
    /**
     * Returns whether the double-score effect is currently active.
     *
     * @return {@code true} if double score is active
     */
    public boolean isDoubleScoreActive() { return doubleScoreActive; }

    /**
     * Returns the tick when the double-score effect ends.
     *
     * @return end tick
     */
    public long getDoubleScoreEndTime() { return doubleScoreEndTick; }
    /**
     * Returns whether the slowdown effect is currently active.
     *
     * @return {@code true} if slowed
     */
    public boolean isSlowed() { return slowed; }
    /**
     * Returns the tick when the slowdown effect ends.
     *
     * @return end tick
     */
    public long getSlowEndTime() { return slowEndTick; }
    /**
     * Returns whether reverse-controls is currently active.
     *
     * @return {@code true} if controls are reversed
     */
    public boolean isReversedControls() { return reversedControls; }
    /**
     * Returns the static obstacles currently in the world.
     *
     * @return obstacle positions (pixel coordinates)
     */
    public List<Point> getObstacles() { return obstacles; }
    /**
     * Returns the current transient unlock message, if any.
     *
     * @return unlock message or {@code null}
     */
    public String getUnlockMessage() { return unlockMessage; }
    /**
     * Returns the list of moving obstacles.
     *
     * @return moving obstacles
     */
    public List<MovingObstacle> getMovingObstacles() { return movingObstacles; }
    /**
     * Returns the current map id for map-based modes.
     *
     * @return current map id
     */
    public int getCurrentMapId() { return currentMapId; }
    /**
     * Returns the frozen settings snapshot captured for this run.
     *
     * @return run settings snapshot
     */
    public SettingsSnapshot getRunSettingsSnapshot() { return runSettingsSnapshot; }
    /**
     * Returns and clears the events emitted during the last tick.
     *
     * @return list of events (possibly empty)
     */
    public List<GameEvent> consumeEvents() {
        if (pendingEvents.isEmpty()) return List.of();
        List<GameEvent> out = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return out;
    }

    /**
     * Applies a direction change to the snake.
     *
     * @param direction new direction
     */
    public void setDirection(Direction direction) { snake.setDirection(direction); }

    /**
     * Returns the tick when the reverse-controls effect ends.
     *
     * @return end tick
     */
    public long getReverseEndTime() { return reverseEndTick; }

    /**
     * Returns the number of apples eaten in the current run.
     *
     * @return apples eaten
     */
    public int getApplesEaten() { return applesEaten; }
    /**
     * Returns the current tick duration in milliseconds.
     *
     * @return tick duration in milliseconds
     */
    public int getTickMs() { return tickMs; }
    /**
     * Returns the accumulated simulation time in milliseconds.
     *
     * @return elapsed simulation time in milliseconds
     */
    public long getElapsedSimTimeMs() { return elapsedSimTimeMs; }

    /**
     * Restores this game state from a saved snapshot.
     *
     * <p>This method updates global {@link GameSettings} from the snapshot and aligns this instance's
     * frozen run settings and map id with the loaded data.</p>
     *
     * @param snap snapshot to restore
     */
    public void restore(GameSnapshot snap) {
        if (snap.settingsSnapshot != null) {
            GameSettings.restore(snap.settingsSnapshot);
        }
        // Snapshot may contain an older selectedMapId; the GameSnapshot field is authoritative for continues.
        GameSettings.setCurrentMode(snap.mode);
        GameSettings.setSelectedMapId(snap.selectedMapId);

        // Align this instance's frozen run settings with the loaded snapshot.
        if (snap.settingsSnapshot != null) {
            this.runSettingsSnapshot = snap.settingsSnapshot;
        }
        this.currentMapId = snap.selectedMapId;

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
        this.elapsedSimTimeMs = 0;
    }
}
