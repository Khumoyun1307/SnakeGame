package test.snakegame;

import com.snakegame.util.ProgressManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProgressManagerTest {
    private static final Path PROGRESS_PATH = Path.of("data/progress.txt");
    private byte[] original;

    @BeforeEach
    void backupProgress() throws Exception {
        if (Files.exists(PROGRESS_PATH)) {
            original = Files.readAllBytes(PROGRESS_PATH);
        }
        // reset file
        Files.deleteIfExists(PROGRESS_PATH);
        ProgressManager.getUnlockedMaps().clear();
        ProgressManager.unlockMap(1);
    }

    @AfterEach
    void restoreProgress() throws Exception {
        if (original != null) {
            Files.write(PROGRESS_PATH, original);
        }
    }

    @Test
    void testUnlockAndPersistence() {
        assertTrue(ProgressManager.isMapUnlocked(1));
        assertFalse(ProgressManager.isMapUnlocked(2));

        ProgressManager.unlockMap(2);
        assertTrue(ProgressManager.isMapUnlocked(2));

        // Reload
        ProgressManager.load();
        assertTrue(ProgressManager.isMapUnlocked(2));
    }
}
