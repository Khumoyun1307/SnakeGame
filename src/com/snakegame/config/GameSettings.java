package com.snakegame.config;

import com.snakegame.mode.GameMode;

public class GameSettings {
    public enum Difficulty { EASY, NORMAL, HARD, EXPERT, INSANE }

    private static final int BEGINNER_DELAY = 180;
    private static final double DIFFICULTY_CONVERSION_MULTIPLIER = 2.4;

    private static Difficulty difficulty = Difficulty.NORMAL;
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

    // Difficulty
    public static Difficulty getDifficulty() { return difficulty; }
    public static void setDifficulty(Difficulty d) { difficulty = d; GameSettingsManager.save(); }

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
}
