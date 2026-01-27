package com.snakegame.config;

import com.snakegame.mode.GameMode;
import com.snakegame.model.GameConfig;
import com.snakegame.ai.AiMode;
import java.util.UUID;

/**
 * Global, user-configurable settings for the game.
 *
 * <p>This class acts as an in-memory singleton: settings are stored in static fields and are
 * persisted via {@link GameSettingsManager}. Most setters trigger an automatic save; use
 * {@link #withAutosaveSuppressed(Runnable)} when applying multiple changes at once.</p>
 */
public class GameSettings {
    /**
     * Coarse difficulty labels derived from the fine-grained {@link #difficultyLevel} value.
     */
    public enum Difficulty { EASY, NORMAL, HARD, EXPERT, INSANE }
    /**
     * Rendering themes used by the UI.
     */
    public enum Theme { RETRO, NEON, PIXEL_ART }

    private static AiMode aiMode = AiMode.SAFE;
    private static boolean autosaveEnabled = true;

    private static final int BEGINNER_DELAY = 180;
    private static final double DIFFICULTY_CONVERSION_MULTIPLIER = 2.4;

    private static Difficulty difficulty = Difficulty.NORMAL;
    private static Theme selectedTheme = Theme.RETRO;

    private static boolean obstaclesEnabled = false;
    private static int difficultyLevel = 20;

    private static GameMode currentMode = GameMode.STANDARD;
    private static int selectedMapId = 1;
    private static int raceThreshold = 20;

    private static boolean soundEnabled = true;
    private static boolean musicEnabled = true;
    private static boolean showGrid = true;
    private static String playerName = "Player";
    private static UUID playerId;

    private static boolean movingObstaclesEnabled = false;
    private static int movingObstacleCount = GameConfig.DEFAULT_MOVING_OBSTACLE_COUNT;
    private static boolean movingObstaclesAutoIncrement = false;

    private static boolean developerModeEnabled = false;

    /**
     * Runs an action while temporarily suppressing the autosave side effect.
     *
     * <p>Useful for bulk updates (e.g., loading from disk) where multiple setters would otherwise
     * trigger repeated writes.</p>
     *
     * @param action action to execute
     */
    public static void withAutosaveSuppressed(Runnable action) {
        boolean prev = autosaveEnabled;
        autosaveEnabled = false;
        try {
            action.run();
        } finally {
            autosaveEnabled = prev;
        }
    }

    private static void saveIfEnabled() {
        if (autosaveEnabled) {
            GameSettingsManager.save();
        }
    }

    /**
     * Returns the coarse difficulty label.
     *
     * @return difficulty label
     */
    public static Difficulty getDifficulty() { return difficulty; }
    /**
     * Sets the coarse difficulty label.
     *
     * @param d new difficulty label
     */
    public static void setDifficulty(Difficulty d) { difficulty = d; saveIfEnabled(); }

    /**
     * Returns the fine-grained difficulty slider level.
     *
     * @return difficulty level in the range 0..50
     */
    public static int getDifficultyLevel() { return difficultyLevel; }

    /**
     * Sets the fine-grained difficulty level and keeps {@link #difficulty} aligned with the slider.
     *
     * @param level requested difficulty level (clamped to 0..50)
     */
    public static void setDifficultyLevel(int level) {
        difficultyLevel = Math.max(0, Math.min(50, level));

        // FIX: keep enum Difficulty aligned with slider levels
        if (difficultyLevel < 10) difficulty = Difficulty.EASY;        // Beginner -> EASY
        else if (difficultyLevel < 20) difficulty = Difficulty.EASY;   // Easy
        else if (difficultyLevel < 30) difficulty = Difficulty.NORMAL; // Medium
        else if (difficultyLevel < 40) difficulty = Difficulty.HARD;   // Hard
        else if (difficultyLevel < 50) difficulty = Difficulty.EXPERT; // Expert
        else difficulty = Difficulty.INSANE;                           // Insane

        saveIfEnabled();
    }

    /**
     * Returns whether random static obstacles are enabled for STANDARD mode.
     *
     * @return {@code true} if obstacles are enabled
     */
    public static boolean isObstaclesEnabled() { return obstaclesEnabled; }
    /**
     * Enables or disables random static obstacles.
     *
     * @param enabled whether obstacles should be enabled
     */
    public static void setObstaclesEnabled(boolean enabled) {
        obstaclesEnabled = enabled;
        saveIfEnabled();
    }

    /**
     * Returns the currently selected mode.
     *
     * @return current game mode
     */
    public static GameMode getCurrentMode() { return currentMode; }
    /**
     * Sets the current mode (defaults to {@link GameMode#STANDARD} when {@code null}).
     *
     * @param mode desired mode
     */
    public static void setCurrentMode(GameMode mode) {
        if (mode == null) {
            mode = GameMode.STANDARD;
        }
        currentMode = mode;
        saveIfEnabled();
    }

    /**
     * Returns the selected map id used for map-based modes.
     *
     * @return selected map id
     */
    public static int getSelectedMapId() { return selectedMapId; }
    /**
     * Sets the selected map id used for map-based modes.
     *
     * @param id map id
     */
    public static void setSelectedMapId(int id) {
        selectedMapId = id;
        saveIfEnabled();
    }

    /**
     * Returns the apple-eaten threshold used to advance maps in {@link GameMode#RACE}.
     *
     * @return race threshold
     */
    public static int getRaceThreshold() { return raceThreshold; }
    /**
     * Sets the race threshold used to advance maps in {@link GameMode#RACE}.
     *
     * @param threshold new threshold
     */
    public static void setRaceThreshold(int threshold) {
        raceThreshold = threshold;
        saveIfEnabled();
    }

    /**
     * Returns whether sound effects are enabled.
     *
     * @return {@code true} if sound effects are enabled
     */
    public static boolean isSoundEnabled() { return soundEnabled; }
    /**
     * Enables or disables sound effects.
     *
     * @param enabled whether sound effects are enabled
     */
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        saveIfEnabled();
    }

    /**
     * Returns whether background music is enabled.
     *
     * @return {@code true} if music is enabled
     */
    public static boolean isMusicEnabled() { return musicEnabled; }
    /**
     * Enables or disables background music.
     *
     * @param enabled whether music is enabled
     */
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        saveIfEnabled();
    }

    /**
     * Returns whether the grid overlay is shown by the renderer.
     *
     * @return {@code true} if the grid is shown
     */
    public static boolean isShowGrid() { return showGrid; }
    /**
     * Shows or hides the grid overlay.
     *
     * @param show whether to show the grid
     */
    public static void setShowGrid(boolean show) {
        showGrid = show;
        saveIfEnabled();
    }

    /**
     * Returns the current player display name.
     *
     * @return player name
     */
    public static String getPlayerName() { return playerName; }
    /**
     * Sets the player display name (ignored if {@code null} or empty).
     *
     * @param name new player name
     */
    public static void setPlayerName(String name) {
        if (name != null && !name.isEmpty()) {
            playerName = name;
            saveIfEnabled();
        }
    }

    /**
     * Returns the persistent player identifier, generating one if missing.
     *
     * @return player UUID
     */
    public static UUID getPlayerId() { return ensurePlayerId(); }
    /**
     * Sets the persistent player identifier (ignored if {@code null}).
     *
     * @param id player UUID
     */
    public static void setPlayerId(UUID id) {
        if (id != null) {
            playerId = id;
            saveIfEnabled();
        }
    }

    /**
     * Ensures a player identifier exists, generating a new UUID if needed.
     *
     * @return ensured player UUID
     */
    public static UUID ensurePlayerId() {
        if (playerId == null) {
            playerId = UUID.randomUUID();
        }
        return playerId;
    }

    /**
     * Computes the base tick delay from the current {@link #difficultyLevel}.
     *
     * @return tick delay in milliseconds
     */
    public static int getSpeedDelayFromDifficultyLevel() {
        return (int) (BEGINNER_DELAY - (difficultyLevel * DIFFICULTY_CONVERSION_MULTIPLIER));
    }

    /**
     * Computes the base tick delay from the provided difficulty level.
     *
     * @param difficultyLevel difficulty slider value (clamped to 0..50)
     * @return tick delay in milliseconds
     */
    public static int speedDelayFromDifficultyLevel(int difficultyLevel) {
        int lvl = Math.max(0, Math.min(50, difficultyLevel));
        return (int) (BEGINNER_DELAY - (lvl * DIFFICULTY_CONVERSION_MULTIPLIER));
    }

    /**
     * Returns the selected rendering theme.
     *
     * @return selected theme
     */
    public static Theme getSelectedTheme() { return selectedTheme; }
    /**
     * Sets the selected rendering theme.
     *
     * @param theme theme to use
     */
    public static void setSelectedTheme(Theme theme) {
        selectedTheme = theme;
        saveIfEnabled();
    }

    /**
     * Returns whether moving obstacles are enabled.
     *
     * @return {@code true} if moving obstacles are enabled
     */
    public static boolean isMovingObstaclesEnabled() { return movingObstaclesEnabled; }
    /**
     * Enables or disables moving obstacles.
     *
     * @param enabled whether moving obstacles are enabled
     */
    public static void setMovingObstaclesEnabled(boolean enabled) {
        movingObstaclesEnabled = enabled;
        saveIfEnabled();
    }

    /**
     * Returns the configured moving obstacle count.
     *
     * @return moving obstacle count
     */
    public static int getMovingObstacleCount() { return movingObstacleCount; }
    /**
     * Sets the moving obstacle count, clamped to a safe range.
     *
     * @param count desired count
     */
    public static void setMovingObstacleCount(int count) {
        movingObstacleCount = Math.max(0, Math.min(GameConfig.DEFAULT_MOVING_OBSTACLE_COUNT, count));
        saveIfEnabled();
    }

    /**
     * Returns whether moving obstacles should auto-increment as the run progresses.
     *
     * @return {@code true} if auto-increment is enabled
     */
    public static boolean isMovingObstaclesAutoIncrement() { return movingObstaclesAutoIncrement; }
    /**
     * Enables or disables auto-incrementing moving obstacles.
     *
     * @param auto whether auto-increment is enabled
     */
    public static void setMovingObstaclesAutoIncrement(boolean auto) {
        movingObstaclesAutoIncrement = auto;
        saveIfEnabled();
    }

    /**
     * Returns whether developer-only features (such as the map editor) are enabled.
     *
     * @return {@code true} if developer mode is enabled
     */
    public static boolean isDeveloperModeEnabled() { return developerModeEnabled; }
    /**
     * Enables or disables developer mode for the current session.
     *
     * <p>Developer mode is intentionally session-only and is not autosaved.</p>
     *
     * @param enabled whether developer mode is enabled
     */
    public static void setDeveloperModeEnabled(boolean enabled) { developerModeEnabled = enabled; }

    /**
     * Captures the current settings into an immutable snapshot.
     *
     * <p>The snapshot contains gameplay-relevant settings used for deterministic saves/replays.
     * Developer mode is not persisted and is always stored as {@code false}.</p>
     *
     * @return settings snapshot
     */
    public static SettingsSnapshot snapshot() {
        return new SettingsSnapshot(
                difficultyLevel,
                obstaclesEnabled,
                currentMode,
                selectedMapId,
                raceThreshold,
                soundEnabled,
                musicEnabled,
                showGrid,
                playerName,
                getPlayerId(),
                selectedTheme,
                movingObstaclesEnabled,
                movingObstacleCount,
                movingObstaclesAutoIncrement,
                false
        );
    }

    /**
     * Restores settings from a previously captured snapshot.
     *
     * @param s snapshot to restore
     */
    public static void restore(SettingsSnapshot s) {
        difficultyLevel               = s.difficultyLevel();
        obstaclesEnabled             = s.obstaclesEnabled();
        currentMode                  = s.currentMode();
        selectedMapId                = s.selectedMapId();
        raceThreshold                = s.raceThreshold();
        soundEnabled                 = s.soundEnabled();
        musicEnabled                 = s.musicEnabled();
        showGrid                     = s.showGrid();
        playerName                   = s.playerName();
        playerId                     = s.playerId();
        selectedTheme                = s.selectedTheme();
        movingObstaclesEnabled       = s.movingObstaclesEnabled();
        movingObstacleCount          = s.movingObstacleCount();
        movingObstaclesAutoIncrement = s.movingObstaclesAutoIncrement();
    }

    /**
     * Returns the currently selected AI strategy.
     *
     * @return AI mode
     */
    public static AiMode getAiMode() { return aiMode; }
    /**
     * Sets the AI strategy (defaults to {@link AiMode#SAFE} when {@code null}).
     *
     * @param mode desired AI mode
     */
    public static void setAiMode(AiMode mode) {
        if (mode == null) mode = AiMode.SAFE;
        aiMode = mode;
        saveIfEnabled();
    }
}
