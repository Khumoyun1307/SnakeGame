package com.snakegame.sound;

import com.snakegame.config.GameSettings;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for playing short sound effects from the {@code /sounds} resource directory.
 *
 * <p>Playback respects {@link com.snakegame.config.GameSettings#isSoundEnabled()}.</p>
 */
public class SoundPlayer {
    private static final Logger log = Logger.getLogger(SoundPlayer.class.getName());

    /**
     * Plays a sound effect asynchronously using a {@link Clip}.
     *
     * @param soundFileName file name under {@code /sounds} (e.g., {@code eatApple.wav})
     */
    public static void play(String soundFileName) {
        // Respect user setting for sound effects
        if (!GameSettings.isSoundEnabled()) return;

        try {
            URL url = SoundPlayer.class.getResource("/sounds/" + soundFileName);
            if (url == null) {
                log.warning("Sound not found: " + soundFileName);
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
            log.log(Level.WARNING, "Failed to play sound: " + soundFileName, e);
        }
    }
}
