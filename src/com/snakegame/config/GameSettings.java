package com.snakegame.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class GameSettings {
    public enum Difficulty { EASY, NORMAL, HARD, EXPERT, INSANE }

    private static final int BEGINNER_DELAY = 180;
    private static final double DIFFICULTY_CONVERSION_MULTIPLIER = 2.4;
    private static Difficulty difficulty = Difficulty.NORMAL;
    private static boolean obstaclesEnabled = false;
    private static int difficultyLevel = 20;

    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(Difficulty d) {
        difficulty = d;
    }

    public static boolean isObstaclesEnabled() {
        return obstaclesEnabled;
    }

    public static void setObstaclesEnabled(boolean enabled) {
        obstaclesEnabled = enabled;
        GameSettingsManager.save(); // <-- add this line
    }

    public static int getDifficultyLevel() {
        return difficultyLevel;
    }

    public static void setDifficultyLevel(int level) {
        difficultyLevel = Math.max(0, Math.min(50, level));
        GameSettingsManager.save();
    }


    public static int getSpeedDelayFromDifficultyLevel() {
        return (int) (BEGINNER_DELAY - (difficultyLevel * DIFFICULTY_CONVERSION_MULTIPLIER));
    }

    // You can later expand with levels, modes, etc.
}
