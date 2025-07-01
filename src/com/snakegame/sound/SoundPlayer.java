package com.snakegame.sound;

import com.snakegame.config.GameSettings;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {
    public static void play(String soundFileName) {
        // Respect user setting for sound effects
        if (!GameSettings.isSoundEnabled()) return;

        try {
            URL url = SoundPlayer.class.getResource("/sounds/" + soundFileName);
            if (url == null) {
                System.err.println("Sound not found: " + soundFileName);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();

            // Close clip after playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
