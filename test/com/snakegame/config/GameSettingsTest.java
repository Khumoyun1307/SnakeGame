package test.snakegame;

import com.snakegame.config.GameSettings;
import com.snakegame.config.GameSettingsManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSettingsTest {
    private static final Path SETTINGS_PATH = Path.of("data/settings.txt");
    private byte[] original;

    @BeforeEach
    void backupSettings() throws Exception {
        if (Files.exists(SETTINGS_PATH)) {
            original = Files.readAllBytes(SETTINGS_PATH);
        }
    }

    @AfterEach
    void restoreSettings() throws Exception {
        if (original != null) {
            Files.write(SETTINGS_PATH, original);
        }
    }

    @Test
    void testDifficultyPersistence() {
        GameSettings.setDifficultyLevel(45);
        GameSettings.setObstaclesEnabled(true);
        GameSettingsManager.save();

        // Reload settings directly to memory (without additional saves)
        GameSettingsManager.load();

        assertEquals(45, GameSettings.getDifficultyLevel());
        assertTrue(GameSettings.isObstaclesEnabled());
    }
}