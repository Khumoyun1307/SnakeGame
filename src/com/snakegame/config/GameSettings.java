package com.snakegame.config;

import com.snakegame.mode.GameMode;
import com.snakegame.model.GameConfig;
import com.snakegame.ai.AiMode;
import java.util.UUID;

public class GameSettings {
    public enum Difficulty { EASY, NORMAL, HARD, EXPERT, INSANE }
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

    public static Difficulty getDifficulty() { return difficulty; }
    public static void setDifficulty(Difficulty d) { difficulty = d; saveIfEnabled(); }

    public static int getDifficultyLevel() { return difficultyLevel; }

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

    public static boolean isObstaclesEnabled() { return obstaclesEnabled; }
    public static void setObstaclesEnabled(boolean enabled) {
        obstaclesEnabled = enabled;
        saveIfEnabled();
    }

    public static GameMode getCurrentMode() { return currentMode; }
    public static void setCurrentMode(GameMode mode) {
        if (mode == null) {
            mode = GameMode.STANDARD;
        }
        currentMode = mode;
        saveIfEnabled();
    }

    public static int getSelectedMapId() { return selectedMapId; }
    public static void setSelectedMapId(int id) {
        selectedMapId = id;
        saveIfEnabled();
    }

    public static int getRaceThreshold() { return raceThreshold; }
    public static void setRaceThreshold(int threshold) {
        raceThreshold = threshold;
        saveIfEnabled();
    }

    public static boolean isSoundEnabled() { return soundEnabled; }
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        saveIfEnabled();
    }

    public static boolean isMusicEnabled() { return musicEnabled; }
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        saveIfEnabled();
    }

    public static boolean isShowGrid() { return showGrid; }
    public static void setShowGrid(boolean show) {
        showGrid = show;
        saveIfEnabled();
    }

    public static String getPlayerName() { return playerName; }
    public static void setPlayerName(String name) {
        if (name != null && !name.isEmpty()) {
            playerName = name;
            saveIfEnabled();
        }
    }

    public static UUID getPlayerId() { return ensurePlayerId(); }
    public static void setPlayerId(UUID id) {
        if (id != null) {
            playerId = id;
            saveIfEnabled();
        }
    }

    public static UUID ensurePlayerId() {
        if (playerId == null) {
            playerId = UUID.randomUUID();
        }
        return playerId;
    }

    public static int getSpeedDelayFromDifficultyLevel() {
        return (int) (BEGINNER_DELAY - (difficultyLevel * DIFFICULTY_CONVERSION_MULTIPLIER));
    }

    public static int speedDelayFromDifficultyLevel(int difficultyLevel) {
        int lvl = Math.max(0, Math.min(50, difficultyLevel));
        return (int) (BEGINNER_DELAY - (lvl * DIFFICULTY_CONVERSION_MULTIPLIER));
    }

    public static Theme getSelectedTheme() { return selectedTheme; }
    public static void setSelectedTheme(Theme theme) {
        selectedTheme = theme;
        saveIfEnabled();
    }

    public static boolean isMovingObstaclesEnabled() { return movingObstaclesEnabled; }
    public static void setMovingObstaclesEnabled(boolean enabled) {
        movingObstaclesEnabled = enabled;
        saveIfEnabled();
    }

    public static int getMovingObstacleCount() { return movingObstacleCount; }
    public static void setMovingObstacleCount(int count) {
        movingObstacleCount = Math.max(0, Math.min(GameConfig.DEFAULT_MOVING_OBSTACLE_COUNT, count));
        saveIfEnabled();
    }

    public static boolean isMovingObstaclesAutoIncrement() { return movingObstaclesAutoIncrement; }
    public static void setMovingObstaclesAutoIncrement(boolean auto) {
        movingObstaclesAutoIncrement = auto;
        saveIfEnabled();
    }

    public static boolean isDeveloperModeEnabled() { return developerModeEnabled; }
    public static void setDeveloperModeEnabled(boolean enabled) { developerModeEnabled = enabled; }

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

    public static AiMode getAiMode() { return aiMode; }
    public static void setAiMode(AiMode mode) {
        if (mode == null) mode = AiMode.SAFE;
        aiMode = mode;
        saveIfEnabled();
    }
}
