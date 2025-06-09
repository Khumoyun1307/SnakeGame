package com.snakegame.ui;

import javax.swing.*;
import java.awt.*;

public class ModePanel extends JPanel {

    public ModePanel(Runnable goBack) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("🗺 Game Modes");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel message = new JLabel("Coming soon...");
        message.setFont(new Font("Arial", Font.ITALIC, 22));
        message.setForeground(Color.LIGHT_GRAY);
        message.setAlignmentX(CENTER_ALIGNMENT);

        JButton back = new JButton("← Back");
        back.setAlignmentX(CENTER_ALIGNMENT);
        back.addActionListener(e -> goBack.run());

        add(title);
        add(Box.createVerticalStrut(20));
        add(message);
        add(Box.createVerticalStrut(30));
        add(back);
    }
}
