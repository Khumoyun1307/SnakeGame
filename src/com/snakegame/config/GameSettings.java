package com.snakegame.config;

import com.snakegame.mode.GameMode;

public class GameSettings {
    public enum Difficulty { EASY, NORMAL, HARD, EXPERT, INSANE }

    private static final int BEGINNER_DELAY = 180;
    private static final double DIFFICULTY_CONVERSION_MULTIPLIER = 2.4;

    private static Difficulty difficulty = Difficulty.NORMAL;
    private static boolean obstaclesEnabled = false;
    private static int difficultyLevel = 20;

    // New mode settings
    private static GameMode currentMode = GameMode.STANDARD;
    private static int selectedMapId = 1;
    private static int raceThreshold = 20; // apples to advance in RACE mode

    public static Difficulty getDifficulty() { return difficulty; }
    public static void setDifficulty(Difficulty d) { difficulty = d; }

    public static boolean isObstaclesEnabled() { return obstaclesEnabled; }
    public static void setObstaclesEnabled(boolean enabled) {
        obstaclesEnabled = enabled;
        GameSettingsManager.save();
    }

    public static int getDifficultyLevel() { return difficultyLevel; }
    public static void setDifficultyLevel(int level) {
        difficultyLevel = Math.max(0, Math.min(50, level));
        GameSettingsManager.save();
    }

    // New getters/setters for modes
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

    public static int getSpeedDelayFromDifficultyLevel() {
        return (int) (BEGINNER_DELAY - (difficultyLevel * DIFFICULTY_CONVERSION_MULTIPLIER));
    }
}