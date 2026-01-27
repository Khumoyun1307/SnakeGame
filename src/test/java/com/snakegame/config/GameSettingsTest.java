package com.snakegame.config;

import com.snakegame.ai.AiMode;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameSettings}.
 */
class GameSettingsTest {

    @Test
    void setDifficultyLevel_clampsAndUpdatesDifficultyEnum() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.setDifficultyLevel(-5);
                assertEquals(0, GameSettings.getDifficultyLevel());
                assertEquals(GameSettings.Difficulty.EASY, GameSettings.getDifficulty());

                GameSettings.setDifficultyLevel(19);
                assertEquals(GameSettings.Difficulty.EASY, GameSettings.getDifficulty());

                GameSettings.setDifficultyLevel(20);
                assertEquals(GameSettings.Difficulty.NORMAL, GameSettings.getDifficulty());

                GameSettings.setDifficultyLevel(30);
                assertEquals(GameSettings.Difficulty.HARD, GameSettings.getDifficulty());

                GameSettings.setDifficultyLevel(40);
                assertEquals(GameSettings.Difficulty.EXPERT, GameSettings.getDifficulty());

                GameSettings.setDifficultyLevel(50);
                assertEquals(GameSettings.Difficulty.INSANE, GameSettings.getDifficulty());

                GameSettings.setDifficultyLevel(999);
                assertEquals(50, GameSettings.getDifficultyLevel());
            });
        }
    }

    @Test
    void speedDelayFromDifficultyLevel_matchesFormula() {
        assertEquals(132, GameSettings.speedDelayFromDifficultyLevel(20)); // 180 - (20*2.4)=132
        assertEquals(60, GameSettings.speedDelayFromDifficultyLevel(50));  // 180 - (50*2.4)=60
    }

    @Test
    void ensurePlayerId_isStableOnceGenerated() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            var id1 = GameSettings.ensurePlayerId();
            var id2 = GameSettings.ensurePlayerId();
            assertNotNull(id1);
            assertEquals(id1, id2);
        }
    }

    @Test
    void snapshot_and_restore_roundTripKeyFields() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.setCurrentMode(GameMode.RACE);
                GameSettings.setSelectedMapId(3);
                GameSettings.setRaceThreshold(7);
                GameSettings.setAiMode(AiMode.SURVIVAL);
            });

            SettingsSnapshot snap = GameSettings.snapshot();

            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.setCurrentMode(GameMode.STANDARD);
                GameSettings.setSelectedMapId(1);
                GameSettings.setRaceThreshold(20);
                GameSettings.setAiMode(AiMode.SAFE);
            });

            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.restore(snap);
                GameSettings.setAiMode(AiMode.SURVIVAL);
            });

            assertEquals(GameMode.RACE, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());
            assertEquals(7, GameSettings.getRaceThreshold());
            assertEquals(AiMode.SURVIVAL, GameSettings.getAiMode());
        }
    }
}
