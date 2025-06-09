package com.snakegame.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class BackgroundMusicPlayer {
    private static Clip clip;

    public static void play(String fileName, boolean loop) {
        try {
            URL soundURL = BackgroundMusicPlayer.class.getResource("/sounds/" + fileName);
            if (soundURL == null) return;

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}
