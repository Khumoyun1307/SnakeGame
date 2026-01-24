package com.snakegame.testutil;

import com.snakegame.ai.AiMode;
import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;

/**
 * Captures and restores global {@link GameSettings} state for isolation between tests.
 */
public final class SettingsGuard implements AutoCloseable {
    private final SettingsSnapshot snapshot;
    private final AiMode aiMode;
    private final boolean developerModeEnabled;

    public SettingsGuard() {
        this.snapshot = GameSettings.snapshot();
        this.aiMode = GameSettings.getAiMode();
        this.developerModeEnabled = GameSettings.isDeveloperModeEnabled();
    }

    @Override
    public void close() {
        GameSettings.withAutosaveSuppressed(() -> {
            GameSettings.restore(snapshot);
            GameSettings.setAiMode(aiMode);
            GameSettings.setDeveloperModeEnabled(developerModeEnabled);
        });
    }
}

