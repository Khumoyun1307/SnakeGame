package com.snakegame.sound;

import com.snakegame.config.GameSettings;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class BackgroundMusicPlayer {
    private static Clip clip;

    /**
     * Plays background music if enabled in settings. Stops any existing clip if music is disabled.
     * @param fileName name of the music file in /sounds
     * @param loop whether to loop continuously
     */
    public static void play(String fileName, boolean loop) {
        // Respect user setting for background music
        if (!GameSettings.isMusicEnabled()) {
            stop();
            return;
        }
        try {
            URL soundURL = BackgroundMusicPlayer.class.getResource("/sounds/" + fileName);
            if (soundURL == null) return;

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /** Stops the music if running. */
    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    /**
     * Refreshes the current track: stops and restarts based on settings.
     */
    public static void refresh(String fileName, boolean loop) {
        stop();
        if (GameSettings.isMusicEnabled()) {
            play(fileName, loop);
        }
    }
}