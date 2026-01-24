package com.snakegame.controller;

import com.snakegame.model.GameEvent;

import java.util.List;

/**
 * Handles domain events emitted by {@link com.snakegame.model.GameState} after each tick.
 */
public interface TickHandler {
    void handleTickEvents(List<GameEvent> events);
    void onGameOver();
}

