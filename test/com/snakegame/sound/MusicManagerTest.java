package com.snakegame.sound;

import com.snakegame.config.GameSettings;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MusicManagerTest {
    @BeforeEach
    void disableMusic() {
        GameSettings.setMusicEnabled(false);
    }

    @AfterEach
    void restoreDefaults() {
        GameSettings.setMusicEnabled(true);
    }

    @Test
    void update_whenMusicDisabled_doesNotThrow() {
        assertDoesNotThrow(() -> MusicManager.update(MusicManager.Screen.MAIN_MENU));
        assertDoesNotThrow(() -> MusicManager.update(MusicManager.Screen.GAMEPLAY));
    }

    @Test
    void stop_always_doesNotThrow() {
        assertDoesNotThrow(MusicManager::stop);
    }
}