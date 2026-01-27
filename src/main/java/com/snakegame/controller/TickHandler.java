package com.snakegame.controller;

import com.snakegame.model.GameEvent;

import java.util.List;

/**
 * Handles domain events emitted by {@link com.snakegame.model.GameState} after each tick.
 */
public interface TickHandler {
    /**
     * Handles the list of events emitted by the simulation for the last tick.
     *
     * @param events events emitted during the last tick
     */
    void handleTickEvents(List<GameEvent> events);
    /**
     * Called when the simulation transitions to game over.
     */
    void onGameOver();
}
