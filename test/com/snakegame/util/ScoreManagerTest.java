package com.snakegame.util;

import com.snakegame.model.GameState;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.util.ScoreManager}.
 */
class ScoreManagerTest {

    @TempDir
    Path tmp;

    @Test
    void addScore_appendsToConfiguredFile_and_updatesHighScore() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path scoreFile = tmp.resolve("scores.txt");
            ScoreManager.setScoreFilePath(scoreFile.toString());
            ScoreManager.clearScores();

            ScoreManager.addScore(10);
            ScoreManager.addScore(5);

            assertEquals(10, ScoreManager.getHighScore());
            assertTrue(Files.exists(scoreFile));
            String content = Files.readString(scoreFile);
            assertTrue(content.contains("Score: 10"));
            assertTrue(content.contains("Score: 5"));
        }
    }

    @Test
    void clearScores_resetsHighScoreToZero() throws Exception {
        Path scoreFile = tmp.resolve("scores.txt");
        ScoreManager.setScoreFilePath(scoreFile.toString());
        ScoreManager.clearScores();

        ScoreManager.addScore(7);
        assertEquals(7, ScoreManager.getHighScore());

        ScoreManager.clearScores();
        assertEquals(0, ScoreManager.getHighScore());
    }

    @Test
    void recordFinishedRun_doesNothingForNullOrNonPositiveScores() throws Exception {
        Path scoreFile = tmp.resolve("scores.txt");
        ScoreManager.setScoreFilePath(scoreFile.toString());
        ScoreManager.clearScores();

        ScoreManager.recordFinishedRun(null);
        assertTrue(ScoreManager.getScores().isEmpty());

        GameState state = new GameState(1L);
        // score is 0 for a fresh state
        ScoreManager.recordFinishedRun(state);
        assertTrue(ScoreManager.getScores().isEmpty());
    }
}
