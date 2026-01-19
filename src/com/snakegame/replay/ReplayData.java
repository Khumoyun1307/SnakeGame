package com.snakegame.replay;

import com.snakegame.config.SettingsSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReplayData {
    public long seed;
    public int finalScore;

    public SettingsSnapshot settingsSnapshot;

    public List<ReplayEvent> events = new ArrayList<>();
}
