package com.snakegame.controller.input;

import com.snakegame.model.Direction;
import com.snakegame.model.GameState;

/**
 * Supplies movement decisions to the game loop.
 *
 * <p>AI-controlled runs compute a direction each tick, while player-controlled runs typically return
 * {@code null} because keyboard input is handled by {@link com.snakegame.controller.GameController}.</p>
 */
public interface DirectionProvider {
    /**
     * Returns the next direction to apply for the given simulation state.
     *
     * @param state current simulation state
     * @return the direction to apply, or {@code null} to keep the current direction
     */
    Direction nextDirection(GameState state);
}
