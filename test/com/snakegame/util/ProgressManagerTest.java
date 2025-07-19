package com.snakegame.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProgressManagerTest {
    @TempDir Path tempDir;
    private Path progressFile;

    @BeforeEach
    void setUp() throws Exception {
        // point ProgressManager at a fresh temp file
        progressFile = tempDir.resolve("progress.txt");
        ProgressManager.setFilePath(progressFile.toString());

        // clear in-memory state, then load (should default to map 1)
        ProgressManager.clearUnlockedMaps();
        ProgressManager.load();
    }

    @Test
    void load_createsDefaultMap1Unlocked() {
        Set<Integer> unlocked = ProgressManager.getUnlockedMaps();
        assertEquals(Set.of(1), unlocked, "Default unlocked set should contain only 1");
        assertTrue(ProgressManager.isMapUnlocked(1));
    }

    @Test
    void unlockMap_addsAndPersists() throws Exception {
        // unlock a new map
        ProgressManager.unlockMap(3);
        assertTrue(ProgressManager.isMapUnlocked(3));

        // reload from disk to verify persistence
        ProgressManager.clearUnlockedMaps();
        ProgressManager.load();
        Set<Integer> reloaded = ProgressManager.getUnlockedMaps();

        assertTrue(reloaded.containsAll(Set.of(1, 3)),
                "After reload, both map 1 and map 3 should be unlocked");
    }
}
