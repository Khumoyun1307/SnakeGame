package com.snakegame.sound;

import com.snakegame.config.GameSettings;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundMusicPlayerTest {
    @BeforeEach
    void disableMusic() {
        GameSettings.setMusicEnabled(false);
    }

    @AfterEach
    void restoreDefaults() {
        GameSettings.setMusicEnabled(true);
    }

    @Test
    void play_whenMusicDisabled_doesNothingAndDoesNotThrow() {
        assertDoesNotThrow(() -> BackgroundMusicPlayer.play("menuMusic.wav", true));
    }

    @Test
    void stop_withNoClip_doesNotThrow() {
        assertDoesNotThrow(BackgroundMusicPlayer::stop);
    }

    @Test
    void refresh_whenMusicDisabled_stopsAndDoesNotThrow() {
        // even if a clip existed, stop() should be called but no exception
        assertDoesNotThrow(() -> BackgroundMusicPlayer.refresh("backgroundMusic.wav", false));
    }
}