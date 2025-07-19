package com.snakegame.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreManagerTest {
    @TempDir Path tempDir;
    private Path scoreFile;

    @BeforeEach
    void setUp() throws Exception {
        // point ScoreManager at a fresh temp file
        scoreFile = tempDir.resolve("scores.txt");
        ScoreManager.setScoreFilePath(scoreFile.toString());
        // clear any in-memory entries
        ScoreManager.clearScores();
        // ensure file exists (empty)
        Files.write(scoreFile, List.of(), Files.exists(scoreFile)
                ? new java.nio.file.OpenOption[]{java.nio.file.StandardOpenOption.TRUNCATE_EXISTING}
                : new java.nio.file.OpenOption[]{java.nio.file.StandardOpenOption.CREATE});
    }

    @Test
    void addScore_appendsEntries_andHighScoreIsCorrect() {
        ScoreManager.addScore(10);
        ScoreManager.addScore(25);

        List<String> all = ScoreManager.getScores();
        assertEquals(2, all.size());
        assertTrue(all.get(0).endsWith("Score: 10"), "First entry should be score 10");
        assertTrue(all.get(1).endsWith("Score: 25"), "Second entry should be score 25");

        assertEquals(25, ScoreManager.getHighScore());
    }

    @Test
    void saveAllToFile_andLoadFromFile_reloadsEntries() throws Exception {
        // add and persist
        ScoreManager.addScore(7);
        ScoreManager.addScore(3);
        ScoreManager.saveAllToFile();

        // wipe memory, then reload from disk
        ScoreManager.clearScores();
        ScoreManager.setScoreFilePath(scoreFile.toString());

        List<String> reloaded = ScoreManager.getScores();
        assertEquals(2, reloaded.size());
        assertTrue(reloaded.get(0).endsWith("Score: 7"));
        assertTrue(reloaded.get(1).endsWith("Score: 3"));
    }
}
