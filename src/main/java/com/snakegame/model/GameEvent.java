package com.snakegame.model;

/**
 * Domain events emitted by {@link GameState} for side effects (sound, persistence, UI flow),
 * keeping the core simulation logic free from external concerns.
 */
public interface GameEvent {

    /** Emitted when an apple was eaten during the last update tick. */
    record AppleEaten(AppleType appleType) implements GameEvent { }

    /** Emitted when the run advances to the next map (e.g., RACE mode). */
    record MapAdvanced(int newMapId) implements GameEvent { }

    /** Emitted when the run transitions from running -> game over. */
    record GameOver(int finalScore) implements GameEvent { }
}

