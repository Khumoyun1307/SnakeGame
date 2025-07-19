package com.snakegame.config;

import com.snakegame.mode.GameMode;

public record SettingsSnapshot(
        int difficultyLevel,
        boolean obstaclesEnabled,
        GameMode currentMode,
        int selectedMapId,
        int raceThreshold,
        boolean soundEnabled,
        boolean musicEnabled,
        boolean showGrid,
        String playerName,
        GameSettings.Theme selectedTheme,
        boolean movingObstaclesEnabled,
        int movingObstacleCount,
        boolean movingObstaclesAutoIncrement,
        boolean developerModeEnabled
) {}