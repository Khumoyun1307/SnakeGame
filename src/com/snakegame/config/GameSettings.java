package com.snakegame.config;

import com.snakegame.mode.GameMode;
import com.snakegame.model.GameConfig;

public class GameSettings {
    public enum Difficulty { EASY, NORMAL, HARD, EXPERT, INSANE }
    public enum Theme {
        RETRO,   // classic green snake on black
        NEON,    // bright neon colors
        PIXEL_ART // blocky pixel art look
    }

    private static final int BEGINNER_DELAY = 180;
    private static final double DIFFICULTY_CONVERSION_MULTIPLIER = 2.4;

    private static Difficulty difficulty = Difficulty.NORMAL;
    private static Theme selectedTheme = Theme.RETRO;

    private static boolean obstaclesEnabled = false;
    private static int difficultyLevel = 20;

    // Mode settings
    private static GameMode currentMode = GameMode.STANDARD;
    private static int selectedMapId = 1;
    private static int raceThreshold = 20;

    // New settings
    private static boolean soundEnabled = true;
    private static boolean musicEnabled = true;
    private static boolean showGrid = true;
    private static String playerName = "Player";

    // Moving obstacle settings

    private static boolean movingObstaclesEnabled = false;
    private static int movingObstacleCount = GameConfig.DEFAULT_MOVING_OBSTACLE_COUNT;
    private static boolean movingObstaclesAutoIncrement = false;

    // Difficulty
    public static Difficulty getDifficulty() { return difficulty; }
    public static void setDifficulty(Difficulty d) { difficulty = d; GameSettingsManager.save(); }

    // Developer mode

    private static boolean developerModeEnabled = false;

    public static int getDifficultyLevel() { return difficultyLevel; }
    public static void setDifficultyLevel(int level) {
        difficultyLevel = Math.max(0, Math.min(50, level));
        GameSettingsManager.save();
    }

    public static boolean isObstaclesEnabled() { return obstaclesEnabled; }
    public static void setObstaclesEnabled(boolean enabled) {
        obstaclesEnabled = enabled;
        GameSettingsManager.save();
    }

    // Mode getters/setters
    public static GameMode getCurrentMode() { return currentMode; }
    public static void setCurrentMode(GameMode mode) {
        currentMode = mode;
        GameSettingsManager.save();
    }

    public static int getSelectedMapId() { return selectedMapId; }
    public static void setSelectedMapId(int id) {
        selectedMapId = id;
        GameSettingsManager.save();
    }

    public static int getRaceThreshold() { return raceThreshold; }
    public static void setRaceThreshold(int threshold) {
        raceThreshold = threshold;
        GameSettingsManager.save();
    }

    // New settings getters/setters
    public static boolean isSoundEnabled() { return soundEnabled; }
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        GameSettingsManager.save();
    }

    public static boolean isMusicEnabled() { return musicEnabled; }
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        GameSettingsManager.save();
    }

    public static boolean isShowGrid() { return showGrid; }
    public static void setShowGrid(boolean show) {
        showGrid = show;
        GameSettingsManager.save();
    }

    public static String getPlayerName() { return playerName; }
    public static void setPlayerName(String name) {
        if (name != null && !name.isEmpty()) {
            playerName = name;
            GameSettingsManager.save();
        }
    }

    public static int getSpeedDelayFromDifficultyLevel() {
        return (int) (BEGINNER_DELAY - (difficultyLevel * DIFFICULTY_CONVERSION_MULTIPLIER));
    }

    public static Theme getSelectedTheme() {
        return selectedTheme;
    }

    public static void setSelectedTheme(Theme theme) {
        selectedTheme = theme;
        GameSettingsManager.save();
    }

    public static boolean isMovingObstaclesEnabled() {
        return movingObstaclesEnabled;
    }
    public static void setMovingObstaclesEnabled(boolean enabled) {
        movingObstaclesEnabled = enabled;
        GameSettingsManager.save();
    }

    public static int getMovingObstacleCount() {
        return movingObstacleCount;
    }
    public static void setMovingObstacleCount(int count) {
        // clamp between 0 and GameConfig.MOVING_OBSTACLE_COUNT
        movingObstacleCount = Math.max(0, Math.min(GameConfig.DEFAULT_MOVING_OBSTACLE_COUNT, count));
        GameSettingsManager.save();
    }

    public static boolean isMovingObstaclesAutoIncrement() {
        return movingObstaclesAutoIncrement;
    }
    public static void setMovingObstaclesAutoIncrement(boolean auto) {
        movingObstaclesAutoIncrement = auto;
        GameSettingsManager.save();
    }

    public static boolean isDeveloperModeEnabled() {
        return developerModeEnabled;
    }

    public static void setDeveloperModeEnabled(boolean enabled) {
        developerModeEnabled = enabled;
    }

    /**
     * Capture all mutable settings into a snapshot object.
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
                selectedTheme,
                movingObstaclesEnabled,
                movingObstacleCount,
                movingObstaclesAutoIncrement,
                developerModeEnabled
        );
    }

    /**
     * Restore all mutable settings from a previously taken snapshot.
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
        selectedTheme                = s.selectedTheme();
        movingObstaclesEnabled       = s.movingObstaclesEnabled();
        movingObstacleCount          = s.movingObstacleCount();
        movingObstaclesAutoIncrement = s.movingObstaclesAutoIncrement();
        developerModeEnabled         = s.developerModeEnabled();
    }

}
