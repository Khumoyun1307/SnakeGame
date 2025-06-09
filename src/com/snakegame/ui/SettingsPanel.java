package com.snakegame.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SettingsPanel extends JPanel {

    public SettingsPanel(ActionListener onBack) {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.DARK_GRAY);

        // Main content area
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.DARK_GRAY);

        JLabel label = new JLabel("Settings (coming soon)");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalGlue());
        content.add(label);
        content.add(Box.createVerticalGlue());

        // Back button
        JButton backButton = new JButton("⬅ Back to Menu");
        backButton.addActionListener(onBack);

        this.add(content, BorderLayout.CENTER);
        this.add(backButton, BorderLayout.SOUTH);
    }
}
