package com.snakegame.mode;

/**
 * Defines the available play modes: standard map, select a fixed map, or race through maps.
 */
public enum GameMode {
    STANDARD,   // Current behavior:  obstacles based on settings, wrap around walls
    MAP_SELECT, // Play one unlocked map from file
    RACE,        // Progress through maps by reaching certai thresholds
    AI
}