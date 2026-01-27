package com.snakegame.controller;

import com.snakegame.model.Direction;
import com.snakegame.mode.GameMode;
import com.snakegame.replay.ReplayEvent;
import com.snakegame.testutil.Reflect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.controller.RunRecorder}.
 */
class RunRecorderTest {

    @Test
    void recordDirectionChange_recordsOnlyForNonAiRuns() {
        RunRecorder ai = new RunRecorder(GameMode.AI);
        ai.recordDirectionChange(1, Direction.UP);
        @SuppressWarnings("unchecked")
        List<ReplayEvent> aiEvents = (List<ReplayEvent>) Reflect.getField(ai, "recordedEvents");
        assertTrue(aiEvents.isEmpty());

        RunRecorder player = new RunRecorder(GameMode.STANDARD);
        player.recordDirectionChange(5, Direction.LEFT);
        @SuppressWarnings("unchecked")
        List<ReplayEvent> events = (List<ReplayEvent>) Reflect.getField(player, "recordedEvents");
        assertEquals(1, events.size());
        assertEquals(5, events.get(0).tick);
        assertEquals(Direction.LEFT, events.get(0).direction);
    }
}
