package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.model.Direction;
import com.snakegame.model.GameState;
import com.snakegame.replay.ReplayData;
import com.snakegame.replay.ReplayEvent;
import com.snakegame.replay.ReplayManager;

import java.util.ArrayList;

/**
 * Records player input events during a run so it can be replayed deterministically later.
 *
 * <p>AI runs are not recorded. Recorded events are persisted via {@link ReplayManager} when the run
 * ends.</p>
 */
public final class RunRecorder {
    private final GameMode runMode;
    private final ArrayList<ReplayEvent> recordedEvents = new ArrayList<>();

    /**
     * Creates a new recorder for the given run mode.
     *
     * @param runMode mode of the current run
     */
    public RunRecorder(GameMode runMode) {
        this.runMode = runMode;
    }

    /**
     * Records a direction change occurring at a specific simulation tick.
     *
     * @param tick tick when the input was applied
     * @param direction new direction
     */
    public void recordDirectionChange(long tick, Direction direction) {
        if (runMode == GameMode.AI) return;
        recordedEvents.add(new ReplayEvent(tick, direction));
    }

    /**
     * Persists the recorded run to the replay store.
     *
     * @param gameState game state to snapshot run metadata from
     */
    public void saveReplay(GameState gameState) {
        if (runMode == GameMode.AI) return;
        if (gameState == null) return;

        ReplayData data = new ReplayData();
        data.seed = gameState.getSeed();
        data.finalScore = gameState.getScore();
        data.runSettingsSnapshot = gameState.getRunSettingsSnapshot();
        data.startMapId = data.runSettingsSnapshot != null
                ? data.runSettingsSnapshot.selectedMapId()
                : GameSettings.getSelectedMapId();
        data.events = new ArrayList<>(recordedEvents);

        ReplayManager.saveLast(data);
        ReplayManager.saveBestIfHigher(data);
    }
}
