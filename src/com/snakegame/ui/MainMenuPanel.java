package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
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
        JButton raceButton = new JButton("🏁 Race Mode");
        JButton modeButton = new JButton("🗺 Select Map");
        JButton difficultyButton = new JButton("🎯 Difficulty");
        JButton settingsButton = new JButton("⚙ Settings");
        JButton statsButton = new JButton("📊 Stats");
        JButton exitButton = new JButton("❌ Exit");

        playButton.setActionCommand("play");
        raceButton.setActionCommand("race");
        modeButton.setActionCommand("mode");
        difficultyButton.setActionCommand("difficulty");
        settingsButton.setActionCommand("settings");
        statsButton.setActionCommand("stats");
        exitButton.setActionCommand("exit");

        playButton.addActionListener(menuListener);
        raceButton.addActionListener(menuListener);
        modeButton.addActionListener(menuListener);
        difficultyButton.addActionListener(menuListener);
        settingsButton.addActionListener(menuListener);
        statsButton.addActionListener(menuListener);
        exitButton.addActionListener(menuListener);

        gbc.gridy = 0; this.add(playButton, gbc);
        gbc.gridy = 1; this.add(raceButton, gbc);
        gbc.gridy = 2; this.add(modeButton, gbc);
        gbc.gridy = 3; this.add(difficultyButton, gbc);
        gbc.gridy = 4; this.add(settingsButton, gbc);
        gbc.gridy = 5; this.add(statsButton, gbc);
        gbc.gridy = 6; this.add(exitButton, gbc);
    }
}