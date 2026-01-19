package com.snakegame.replay;

import com.snakegame.model.Direction;

public class ReplayEvent {
    public final long tick;
    public final Direction direction;

    public ReplayEvent(long tick, Direction direction) {
        this.tick = tick;
        this.direction = direction;
    }
}
