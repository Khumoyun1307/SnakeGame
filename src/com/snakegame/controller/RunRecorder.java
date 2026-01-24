package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.model.Direction;
import com.snakegame.model.GameState;
import com.snakegame.replay.ReplayData;
import com.snakegame.replay.ReplayEvent;
import com.snakegame.replay.ReplayManager;

import java.util.ArrayList;

public final class RunRecorder {
    private final GameMode runMode;
    private final ArrayList<ReplayEvent> recordedEvents = new ArrayList<>();

    public RunRecorder(GameMode runMode) {
        this.runMode = runMode;
    }

    public void recordDirectionChange(long tick, Direction direction) {
        if (runMode == GameMode.AI) return;
        recordedEvents.add(new ReplayEvent(tick, direction));
    }

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

