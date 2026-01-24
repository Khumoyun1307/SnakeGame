package com.snakegame;

import com.snakegame.config.GameSettingsManager;
import com.snakegame.ui.GameFrame;

import javax.swing.*;

/**
 * Application entry point for the Snake game.
 *
 * <p>Loads persisted settings and launches the Swing UI on the Event Dispatch Thread (EDT).</p>
 */
public class SnakeGame {
    /**
     * Starts the application.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        GameSettingsManager.load();
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
