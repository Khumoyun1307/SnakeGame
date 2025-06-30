package com.snakegame.mode;

/**
 * Defines the available play modes: standard random-map, select a fixed map, or race through maps.
 */
public enum GameMode {
    STANDARD,   // Current behavior: random obstacles based on settings
    MAP_SELECT, // Play one unlocked map from file
    RACE        // Progress through maps by eating apples
}