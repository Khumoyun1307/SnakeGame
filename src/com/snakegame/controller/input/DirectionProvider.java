package com.snakegame.controller.input;

import com.snakegame.model.Direction;
import com.snakegame.model.GameState;

public interface DirectionProvider {
    Direction nextDirection(GameState state);
}