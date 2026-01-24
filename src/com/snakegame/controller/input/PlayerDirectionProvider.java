package com.snakegame.controller.input;

import com.snakegame.model.Direction;
import com.snakegame.model.GameState;

/**
 * Direction provider for player-controlled runs.
 *
 * <p>Keyboard input is handled by {@link com.snakegame.controller.GameController}, so this provider
 * always returns {@code null}.</p>
 */
public class PlayerDirectionProvider implements DirectionProvider {
    @Override
    public Direction nextDirection(GameState state) {
        return null; // keyboard drives it in GameController
    }
}
