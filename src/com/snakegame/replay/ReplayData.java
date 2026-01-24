package com.snakegame.replay;

import com.snakegame.config.SettingsSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReplayData {
    public static final int CURRENT_VERSION = 2;

    /** Replay file format version. */
    public int version = CURRENT_VERSION;

    public long seed;
    public int finalScore;

    /** Settings captured at the START of the run (gameplay-deterministic). */
    public SettingsSnapshot runSettingsSnapshot;

    /**
     * Starting map for MAP_SELECT/RACE at the beginning of the run.
     * (For backwards compatibility, this can fall back to runSettingsSnapshot.selectedMapId().)
     */
    public int startMapId;

    public List<ReplayEvent> events = new ArrayList<>();
}
