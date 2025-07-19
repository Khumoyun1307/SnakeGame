package com.snakegame;

import com.snakegame.config.GameSettingsManager;
import com.snakegame.ui.GameFrame;

import javax.swing.*;

public class SnakeGame {
    public static void main(String[] args) {
        GameSettingsManager.load();
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

