package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.MapManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.snakegame.util.ProgressManager;
public class MainMenuPanel extends JPanel {

    private final ActionListener menuListener;
    private boolean ctrlDown = false;
    private final StringBuilder codeBuffer = new StringBuilder();
    private boolean devButtonAdded = false;
    private JButton continueButton;

    public MainMenuPanel(ActionListener menuListener) {
        this.menuListener = menuListener;
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ✅ Use the FIELD, not a local variable
        this.continueButton = new JButton("▶ Continue");
        this.continueButton.setActionCommand("continue");
//        this.continueButton.addActionListener(menuListener);

        this.continueButton.addActionListener(evt -> {
            menuListener.actionPerformed(evt);
        });

        JButton playButton = new JButton("▶ Play Snake Game");
        JButton raceButton = new JButton("🏁 Race Mode");
        JButton aiButton = new JButton("🤖 AI Mode (A*)");
        JButton modeButton = new JButton("🗺 Select Map");
        JButton replayButton = new JButton("🎬 Replay Games");
        JButton difficultyButton = new JButton("🎯 Difficulty");
        JButton leaderboardButton = new JButton("🏆 Online Leaderboard");
        JButton statsButton = new JButton("📊 Stats");
        JButton settingsButton = new JButton("⚙ Settings");
        JButton exitButton = new JButton("❌ Exit");

        playButton.setActionCommand("play");
        raceButton.setActionCommand("race");
        aiButton.setActionCommand("aiMenu");
        modeButton.setActionCommand("mode");
        replayButton.setActionCommand("replay");
        difficultyButton.setActionCommand("difficulty");
        leaderboardButton.setActionCommand("leaderboard");
        settingsButton.setActionCommand("settings");
        statsButton.setActionCommand("stats");
        exitButton.setActionCommand("exit");

        playButton.addActionListener(menuListener);
        raceButton.addActionListener(menuListener);
        aiButton.addActionListener(menuListener);
        modeButton.addActionListener(menuListener);
        replayButton.addActionListener(menuListener);
        difficultyButton.addActionListener(menuListener);
        leaderboardButton.addActionListener(menuListener);
        statsButton.addActionListener(menuListener);
        settingsButton.addActionListener(menuListener);
        exitButton.addActionListener(menuListener);

        // ✅ Always add Continue first, then everything else shifted down
        gbc.gridy = 0; this.add(this.continueButton, gbc);
        gbc.gridy = 1; this.add(playButton, gbc);
        gbc.gridy = 2; this.add(raceButton, gbc);
        gbc.gridy = 3; this.add(aiButton, gbc);
        gbc.gridy = 4; this.add(modeButton, gbc);
        gbc.gridy = 5; this.add(replayButton, gbc);
        gbc.gridy = 6; this.add(difficultyButton, gbc);
        gbc.gridy = 7; this.add(leaderboardButton, gbc);
        gbc.gridy = 8; this.add(statsButton, gbc);
        gbc.gridy = 9; this.add(settingsButton, gbc);
        gbc.gridy = 10; this.add(exitButton, gbc);

        // ✅ Sets visible/text correctly based on saved game presence
        refreshContinueButton();
        if (GameSettings.isDeveloperModeEnabled()) {
            addDeveloperButton();
        }

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    ctrlDown = true;
                    return;
                }
                if (!ctrlDown) return;

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
                    default              -> { return; }
                }

                String target = "713Developer";
                if (codeBuffer.length() > target.length()) {
                    codeBuffer.delete(0, codeBuffer.length() - target.length());
                }
                if (codeBuffer.toString().equals(target)) {
                    GameSettings.setDeveloperModeEnabled(true);
                    MapManager.loadDeveloperMaps();
                    JOptionPane.showMessageDialog(
                            MainMenuPanel.this,
                            "🔧 Developer mode unlocked!"
                    );
                    addDeveloperButton();
                    refreshContinueButton();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    ctrlDown = false;
                    codeBuffer.setLength(0);
                }
            }
        });
    }


    public void refreshContinueButton() {
        if (ProgressManager.hasSavedGame()
                && (!ProgressManager.isSavedGameDeveloperOnly() || GameSettings.isDeveloperModeEnabled())) {
            int savedScore = ProgressManager.getSavedGameScore().orElse(0);
            continueButton.setText("▶ Continue (" + savedScore + ")");
            continueButton.setVisible(true);
        } else {
            continueButton.setVisible(false);
        }
        revalidate();
        repaint();
    }


    private void addDeveloperButton() {
        if (devButtonAdded) return;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 11; // after Exit
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
