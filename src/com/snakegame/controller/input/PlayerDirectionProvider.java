package com.snakegame.controller.input;

import com.snakegame.model.Direction;
import com.snakegame.model.GameState;

public class PlayerDirectionProvider implements DirectionProvider {
    @Override
    public Direction nextDirection(GameState state) {
        return null; // keyboard drives it in GameController
    }
}
