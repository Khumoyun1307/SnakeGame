package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainMenuPanel extends JPanel {

    private final ActionListener menuListener;
    private boolean ctrlDown = false;
    private final StringBuilder codeBuffer = new StringBuilder();
    private boolean devButtonAdded = false;

    public MainMenuPanel(ActionListener menuListener) {
        this.menuListener = menuListener;
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

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Track Ctrl state
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    ctrlDown = true;
                    return;
                }
                if (!ctrlDown) return;

                // Append the right character for each key
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_7   -> codeBuffer.append('7');
                    case KeyEvent.VK_1   -> codeBuffer.append('1');
                    case KeyEvent.VK_3   -> codeBuffer.append('3');
                    case KeyEvent.VK_D   -> codeBuffer.append('D');
                    case KeyEvent.VK_E   -> codeBuffer.append('e');
                    case KeyEvent.VK_V   -> codeBuffer.append('v');
                    case KeyEvent.VK_L   -> codeBuffer.append('l');
                    case KeyEvent.VK_O   -> codeBuffer.append('o');
                    case KeyEvent.VK_P   -> codeBuffer.append('p');
                    case KeyEvent.VK_R   -> codeBuffer.append('r');
                    default              -> { return; } // ignore everything else
                }

                String target = "713Developer";
                // keep only the last N chars
                if (codeBuffer.length() > target.length()) {
                    codeBuffer.delete(0, codeBuffer.length() - target.length());
                }
                if (codeBuffer.toString().equals(target)) {
                    GameSettings.setDeveloperModeEnabled(true);
                    JOptionPane.showMessageDialog(
                            MainMenuPanel.this,
                            "🔧 Developer mode unlocked!"
                    );
                    addDeveloperButton();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Reset if Ctrl is released
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    ctrlDown = false;
                    codeBuffer.setLength(0);
                }
            }
        });

    }

    private void addDeveloperButton() {
        if (devButtonAdded) return;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 7; // after your existing 0–6 rows
        gbc.gridx = 0;

        JButton dev = new JButton("🛠 Developer");
        dev.setActionCommand("developer");
        dev.addActionListener(menuListener);
        add(dev, gbc);
        devButtonAdded = true;
        revalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();  // grab focus as soon as this panel is displayed
    }

}