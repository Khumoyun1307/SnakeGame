package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.snakegame.sound.MusicManager;

public class SettingsPanel extends JPanel {
    private final JCheckBox soundCheck;
    private final JCheckBox musicCheck;
    private final JCheckBox gridCheck;
    private final JTextField nameField;
    private final JComboBox<GameSettings.Theme> themeCombo;

    boolean wasMusicOn = GameSettings.isMusicEnabled();
    boolean wasSoundOn = GameSettings.isSoundEnabled();

    public SettingsPanel(ActionListener onBack) {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.DARK_GRAY);

        JLabel title = new JLabel("⚙ Settings", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Player Name
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameField = new JTextField(GameSettings.getPlayerName(), 15);
        nameField.setMaximumSize(new Dimension(200, 25));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sound Effects Toggle
        soundCheck = new JCheckBox("Enable Sound Effects");
        soundCheck.setSelected(GameSettings.isSoundEnabled());
        soundCheck.setForeground(Color.WHITE);
        soundCheck.setBackground(Color.DARK_GRAY);
        soundCheck.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Background Music Toggle
        musicCheck = new JCheckBox("Enable Music");
        musicCheck.setSelected(GameSettings.isMusicEnabled());
        musicCheck.setForeground(Color.WHITE);
        musicCheck.setBackground(Color.DARK_GRAY);
        musicCheck.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Grid Display Toggle
        gridCheck = new JCheckBox("Show Grid");
        gridCheck.setSelected(GameSettings.isShowGrid());
        gridCheck.setForeground(Color.WHITE);
        gridCheck.setBackground(Color.DARK_GRAY);
        gridCheck.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Theme menu

        themeCombo = new JComboBox<>(GameSettings.Theme.values());
        themeCombo.setSelectedItem(GameSettings.getSelectedTheme());
        themeCombo.setMaximumSize(new Dimension(200, 25));
        themeCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalStrut(15));
        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        themeLabel.setForeground(Color.WHITE);
        themeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(themeLabel);

        content.add(Box.createVerticalStrut(15));

        content.add(themeCombo);
        content.add(Box.createVerticalStrut(10));


        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.DARK_GRAY);
        JButton save = getSaveJButton();

        JButton back = new JButton("← Back");
        back.addActionListener(onBack);
        buttonPanel.add(save);
        buttonPanel.add(back);

        content.add(Box.createVerticalStrut(20));
        content.add(title);
        content.add(Box.createVerticalStrut(20));
        content.add(nameLabel);
        content.add(nameField);
        content.add(Box.createVerticalStrut(10));
        content.add(soundCheck);
        content.add(musicCheck);
        content.add(gridCheck);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);

        add(content, BorderLayout.CENTER);
    }

    private JButton getSaveJButton() {
        JButton save = new JButton("✔ Save");
        save.addActionListener(e -> {
            GameSettings.setPlayerName(nameField.getText().trim());
            GameSettings.setSoundEnabled(soundCheck.isSelected());
            GameSettings.setMusicEnabled(musicCheck.isSelected());
            GameSettings.setShowGrid(gridCheck.isSelected());
            GameSettings.setSelectedTheme((GameSettings.Theme)themeCombo.getSelectedItem());

            boolean nowMusicOn;
            boolean nowSoundOn;
            nowMusicOn = musicCheck.isSelected();
            nowSoundOn = soundCheck.isSelected();
            MusicManager.update(MusicManager.Screen.GAMEPLAY, nowMusicOn);
            MusicManager.setWasMusicOn(nowMusicOn);
            MusicManager.setWasSoundOn(nowSoundOn);
            wasMusicOn = nowMusicOn;
            wasSoundOn = nowSoundOn;


            JOptionPane.showMessageDialog(this, "Settings saved!");
        });
        return save;
    }
}
