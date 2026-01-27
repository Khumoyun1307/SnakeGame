package com.snakegame.model;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of a run used for save/continue.
 *
 * <p>This object captures enough state to restore gameplay deterministically (snake body, apple,
 * effects, obstacles, and the gameplay-relevant {@link SettingsSnapshot}). Fields are kept public
 * intentionally to keep persistence straightforward.</p>
 */
public class GameSnapshot implements Serializable {
    // Metadata
    public long savedAtMillis;

    // Settings/mode needed to reconstruct correct map behavior
    public GameMode mode;
    public int selectedMapId;

    // Score / progression
    public int score;
    public int applesEaten;

    // Snake
    public Direction direction;
    public List<Point> snakeBody = new ArrayList<>();

    // Apple
    public Point applePos;
    public AppleType appleType;
    public long appleSpawnTime;
    public long appleVisibleDurationMs;

    // Powerups/effects
    public boolean doubleScoreActive;
    public long doubleScoreEndTime;
    public boolean slowed;
    public long slowEndTime;
    public boolean reversedControls;
    public long reverseEndTime;

    // Snapshots
    public SettingsSnapshot settingsSnapshot;
    // Obstacles
    public List<Point> obstacles = new ArrayList<>();
    public List<MovingObstacleSnapshot> movingObstacles = new ArrayList<>();

    /**
     * Returns the score shown on the main menu "Continue" button.
     *
     * @return saved score
     */
    public int continueLabelScore() {
        return score;
    }

    /**
     * Captures a snapshot from the provided {@link GameState}.
     *
     * @param state current game state to snapshot
     * @return snapshot representing the current run state
     */
    public static GameSnapshot captureFrom(GameState state) {
        GameSnapshot s = new GameSnapshot();
        s.savedAtMillis = System.currentTimeMillis();

        s.settingsSnapshot = GameSettings.snapshot();
        s.mode = GameSettings.getCurrentMode();
        s.selectedMapId = state.getCurrentMapId();

        s.score = state.getScore();
        s.applesEaten = state.getApplesEaten();

        s.direction = state.getSnake().getDirection();
        s.snakeBody = new ArrayList<>(state.getSnake().getBody()); // Deque -> List copy

        s.applePos = new Point(state.getApple().getPosition());
        s.appleType = state.getApple().getType();
        s.appleSpawnTime = state.getApple().getSpawnTime();
        s.appleVisibleDurationMs = state.getApple().getVisibleDurationMs();

        s.doubleScoreActive = state.isDoubleScoreActive();
        s.doubleScoreEndTime = state.getDoubleScoreEndTime();
        s.slowed = state.isSlowed();
        s.slowEndTime = state.getSlowEndTime();
        s.reversedControls = state.isReversedControls();
        s.reverseEndTime = state.getReverseEndTime();

        s.obstacles = new ArrayList<>(state.getObstacles());

        for (MovingObstacle mo : state.getMovingObstacles()) {
            s.movingObstacles.add(MovingObstacleSnapshot.from(mo));
        }
        return s;
    }
}
