package com.snakegame.sound;

import com.snakegame.config.GameSettings;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SoundPlayerTest {
    @BeforeEach
    void disableSound() {
        GameSettings.setSoundEnabled(false);
    }

    @AfterEach
    void restoreDefaults() {
        GameSettings.setSoundEnabled(true);
    }

    @Test
    void play_whenSoundDisabled_doesNotThrow() {
        assertDoesNotThrow(() -> SoundPlayer.play("eatApple.wav"));
    }

    @Test
    void play_whenSoundEnabled_doesNotThrow() {
        GameSettings.setSoundEnabled(true);
        assertDoesNotThrow(() -> SoundPlayer.play("eatApple.wav"));
    }
}