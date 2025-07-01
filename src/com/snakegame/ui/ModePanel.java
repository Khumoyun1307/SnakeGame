package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.util.ProgressManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ModePanel extends JPanel {
    private int selectedMap;
    private final Map<Integer, JToggleButton> mapButtons = new HashMap<>();

    public ModePanel(Runnable goBack) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // Determine initial selection: 0 = Basic Map, >0 = selected map
        if (GameSettings.getCurrentMode() == GameMode.STANDARD) {
            selectedMap = 0;
        } else {
            selectedMap = GameSettings.getSelectedMapId();
        }

        // Title
        JLabel title = new JLabel("🗺 Select Map", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Grid of toggle buttons: Basic + maps
        JPanel grid = new JPanel(new GridLayout(0, 5, 10, 10));
        grid.setBackground(Color.BLACK);
        ButtonGroup group = new ButtonGroup();

        // Basic Map option
        JToggleButton basicBtn = new JToggleButton("Basic Map");
        basicBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        basicBtn.setForeground(Color.WHITE);
        basicBtn.setBackground(Color.DARK_GRAY);
        basicBtn.setFocusPainted(false);
        basicBtn.addActionListener(e -> selectedMap = 0);
        group.add(basicBtn);
        mapButtons.put(0, basicBtn);
        grid.add(basicBtn);

        // File-based maps
        for (int i = 1; i <= 10; i++) {
            JToggleButton btn = new JToggleButton("Map " + i);
            btn.setFont(new Font("Arial", Font.PLAIN, 16));
            btn.setForeground(Color.WHITE);
            btn.setBackground(Color.DARK_GRAY);
            btn.setFocusPainted(false);

            boolean unlocked = ProgressManager.isMapUnlocked(i);
            btn.setEnabled(unlocked);
            if (!unlocked) {
                btn.setText("🔒 Map " + i);
            }

            final int mapId = i;
            btn.addActionListener(e -> selectedMap = mapId);
            group.add(btn);
            mapButtons.put(i, btn);
            grid.add(btn);
        }
        add(grid, BorderLayout.CENTER);

        // Preselect the initial button
        JToggleButton initial = mapButtons.get(selectedMap);
        if (initial != null) {
            initial.setSelected(true);
        }

        // Bottom panel: Save and Back
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setBackground(Color.BLACK);

        JButton saveButton = new JButton("✔ Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.addActionListener(e -> {
            if (selectedMap == 0) {
                GameSettings.setCurrentMode(GameMode.STANDARD);
            } else {
                GameSettings.setCurrentMode(GameMode.MAP_SELECT);
                GameSettings.setSelectedMapId(selectedMap);
            }
            goBack.run();
        });

        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.addActionListener(e -> goBack.run());

        bottom.add(saveButton);
        bottom.add(backButton);
        add(bottom, BorderLayout.SOUTH);
    }
}
