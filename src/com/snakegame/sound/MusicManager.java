package com.snakegame.sound;

import com.snakegame.config.GameSettings;

import java.io.FileNotFoundException;

/**
 * Central manager for background music, handling menu vs. gameplay tracks.
 */
public class MusicManager {
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

    public static boolean isWasMusicOn() {
        return wasMusicOn;
    }

    public boolean isWasSoundOn() {
        return wasSoundOn;
    }

    public static void setWasMusicOn(boolean wasMusicOn) {
        MusicManager.wasMusicOn = wasMusicOn;
    }

    public static void setWasSoundOn(boolean wasSoundOn) {
        MusicManager.wasSoundOn = wasSoundOn;
    }
}
