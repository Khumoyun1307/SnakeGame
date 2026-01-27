package com.snakegame.config;

import com.snakegame.mode.GameMode;

/**
 * Immutable snapshot of gameplay-relevant settings.
 *
 * <p>Snapshots are used to keep saves and replays deterministic by freezing the run configuration
 * at the start of play. UI-only and session-only settings are intentionally excluded.</p>
 *
 * @param difficultyLevel fine-grained difficulty slider level (0..50)
 * @param obstaclesEnabled whether random obstacles are enabled
 * @param currentMode active game mode
 * @param selectedMapId selected map id for map-based modes
 * @param raceThreshold apples-eaten threshold to advance maps in RACE mode
 * @param soundEnabled whether sound effects are enabled
 * @param musicEnabled whether background music is enabled
 * @param showGrid whether the grid overlay is displayed
 * @param playerName player display name
 * @param playerId persistent player identifier
 * @param selectedTheme selected UI theme
 * @param movingObstaclesEnabled whether moving obstacles are enabled
 * @param movingObstacleCount number of moving obstacles to spawn
 * @param movingObstaclesAutoIncrement whether moving obstacles auto-increment as the run progresses
 * @param developerModeEnabled whether developer-only features are enabled (typically false in snapshots)
 */
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
        java.util.UUID playerId,
        GameSettings.Theme selectedTheme,
        boolean movingObstaclesEnabled,
        int movingObstacleCount,
        boolean movingObstaclesAutoIncrement,
        boolean developerModeEnabled
) {}
