package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.util.ProgressManager;
import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ModePanel extends JPanel {
    public ModePanel(Runnable goBack) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel title = new JLabel("🗺 Select Map", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 5, 10, 10));
        grid.setBackground(Color.BLACK);

        Set<Integer> unlocked = ProgressManager.getUnlockedMaps();
        for (int i = 1; i <= 10; i++) {
            JButton btn = new JButton("Map " + i);
            btn.setFont(new Font("Arial", Font.PLAIN, 16));
            if (!unlocked.contains(i)) {
                btn.setEnabled(false);
                btn.setText("🔒 Map " + i);
            }
            final int mapId = i;
            btn.addActionListener(e -> {
                GameSettings.setCurrentMode(GameMode.MAP_SELECT);
                GameSettings.setSelectedMapId(mapId);
                goBack.run();
            });
            grid.add(btn);
        }
        add(grid, BorderLayout.CENTER);

        JButton back = new JButton("← Back");
        back.addActionListener(e -> goBack.run());
        JPanel south = new JPanel(); south.setBackground(Color.BLACK); south.add(back);
        add(south, BorderLayout.SOUTH);
    }
}