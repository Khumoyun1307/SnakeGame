package com.snakegame.replay;

import com.snakegame.model.Direction;

/**
 * An input event recorded for deterministic replay.
 *
 * <p>Events are keyed by simulation tick so playback can apply inputs before advancing the state.</p>
 */
public class ReplayEvent {
    public final long tick;
    public final Direction direction;

    /**
     * Creates a replay event.
     *
     * @param tick simulation tick when the input occurred
     * @param direction direction applied at {@code tick}
     */
    public ReplayEvent(long tick, Direction direction) {
        this.tick = tick;
        this.direction = direction;
    }
}
