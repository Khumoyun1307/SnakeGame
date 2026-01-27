package com.snakegame.sound;

import com.snakegame.config.GameSettings;

import java.io.FileNotFoundException;

/**
 * Central manager for background music, handling menu vs. gameplay tracks.
 */
public class MusicManager {
    /**
     * Identifies the current UI context so the appropriate background track can be selected.
     */
    public enum Screen { MAIN_MENU, GAMEPLAY }

    static boolean wasMusicOn = GameSettings.isMusicEnabled();
    static boolean wasSoundOn = GameSettings.isSoundEnabled();
    /**
     * Updates the background track based on the current screen and user setting.
     * @param screen the active UI context (menu or gameplay)
     */
    public static void update(Screen screen) {
        // stop any existing music
        BackgroundMusicPlayer.stop();

        // if music is disabled, don't start anything
        if (!GameSettings.isMusicEnabled()) {
            return;
        }

        // pick the proper music file
        String file;
        switch (screen) {
            case MAIN_MENU:
                file = "menuMusic.wav";
                break;
            case GAMEPLAY:
            default:
                file = "backgroundMusic.wav";
                break;
        }

        // start playback (loops by default)
        BackgroundMusicPlayer.play(file, true);
    }

    /**
     * Updates background music when a settings toggle changes.
     *
     * @param screen current UI context
     * @param nowMusicOn current value of the music-enabled setting
     */
    public static void update(Screen screen, boolean nowMusicOn) {
        if (wasMusicOn != nowMusicOn) {
            if (nowMusicOn) {
                MusicManager.update(screen);
            } else {
                MusicManager.stop();
            }
        }
        wasMusicOn = nowMusicOn;
    }

    /**
     * Stops any background music immediately.
     */
    public static void stop() {
        BackgroundMusicPlayer.stop();
    }

    /**
     * Returns the last observed music-enabled state tracked by the manager.
     *
     * @return whether music was previously enabled
     */
    public static boolean isWasMusicOn() {
        return wasMusicOn;
    }

    /**
     * Returns the last observed sound-enabled state tracked by the manager.
     *
     * @return whether sound effects were previously enabled
     */
    public boolean isWasSoundOn() {
        return wasSoundOn;
    }

    /**
     * Updates the stored previous music-enabled state.
     *
     * @param wasMusicOn previous value of the music-enabled setting
     */
    public static void setWasMusicOn(boolean wasMusicOn) {
        MusicManager.wasMusicOn = wasMusicOn;
    }

    /**
     * Updates the stored previous sound-enabled state.
     *
     * @param wasSoundOn previous value of the sound-enabled setting
     */
    public static void setWasSoundOn(boolean wasSoundOn) {
        MusicManager.wasSoundOn = wasSoundOn;
    }
}
