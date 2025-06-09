package com.snakegame.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {
    public static void play(String soundFileName) {
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

            // Optional: Close clip after playing
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
