package com.snakegame.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(ActionListener menuListener) {
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton playButton = new JButton("▶ Play Snake Game");
        JButton modeButton = new JButton("🗺 Mode (Coming Soon)");
        JButton difficultyButton = new JButton("🎯 Difficulty");
        JButton settingsButton = new JButton("⚙ Settings");
        JButton statsButton = new JButton("📊 Stats");
        JButton exitButton = new JButton("❌ Exit");


        playButton.setActionCommand("play");
        modeButton.setActionCommand("mode");
        difficultyButton.setActionCommand("difficulty");
        settingsButton.setActionCommand("settings");
        statsButton.setActionCommand("stats");
        exitButton.setActionCommand("exit");

        playButton.addActionListener(menuListener);
        settingsButton.addActionListener(menuListener);
        statsButton.addActionListener(menuListener);
        exitButton.addActionListener(menuListener);
        modeButton.addActionListener(menuListener);
        difficultyButton.addActionListener(menuListener);


        gbc.gridy = 0; this.add(playButton, gbc);
        gbc.gridy = 1; this.add(modeButton, gbc);
        gbc.gridy = 2; this.add(difficultyButton, gbc);
        gbc.gridy = 3; this.add(settingsButton, gbc);
        gbc.gridy = 4; this.add(statsButton, gbc);
        gbc.gridy = 5; this.add(exitButton, gbc);
    }
}
