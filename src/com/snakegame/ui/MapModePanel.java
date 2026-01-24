package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapManager;
import com.snakegame.util.ProgressManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map selection panel that uses only the in-memory map list.
 */
public class MapModePanel extends JPanel {
    private int selectedMap;
    private final JPanel grid;
    private final ButtonGroup group;
    private final Map<Integer, JToggleButton> mapButtons;

    /**
     * Creates a map selection panel.
     *
     * @param goBack callback invoked when navigating back
     */
    public MapModePanel(Runnable goBack) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // If dev mode just turned on, load any dev maps into memory
        if (GameSettings.isDeveloperModeEnabled()) {
            MapManager.loadDeveloperMaps();
        }

        // Determine initial selection
        selectedMap = (GameSettings.getCurrentMode() == GameMode.STANDARD)
                ? 0
                : GameSettings.getSelectedMapId();

        // Title
        JLabel title = new JLabel("🗺 Select Map", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Grid container
        grid = new JPanel(new GridLayout(0, 5, 10, 10));
        grid.setBackground(Color.BLACK);
        group = new ButtonGroup();
        mapButtons = new HashMap<>();

        // Populate buttons from memory
        loadButtons();
        add(grid, BorderLayout.CENTER);

        // Pre-select current map
        JToggleButton init = mapButtons.get(selectedMap);
        if (init != null) init.setSelected(true);

        // Bottom save/back
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setBackground(Color.BLACK);
        JButton save = new JButton("✔ Save");
        save.setFont(new Font("Arial", Font.BOLD, 16));
        save.addActionListener(e -> {
            if (selectedMap == 0) {
                GameSettings.setCurrentMode(GameMode.STANDARD);
            } else {
                GameSettings.setCurrentMode(GameMode.MAP_SELECT);
                GameSettings.setSelectedMapId(selectedMap);
            }
            goBack.run();
        });
        JButton back = new JButton("← Back");
        back.setFont(new Font("Arial", Font.PLAIN, 14));
        back.addActionListener(e -> goBack.run());
        bottom.add(save);
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadButtons() {
        grid.removeAll();
        group.clearSelection();
        mapButtons.clear();

        // Basic map
        JToggleButton basic = new JToggleButton("Basic Map");
        styleButton(basic);
        basic.addActionListener(e -> selectedMap = 0);
        group.add(basic); mapButtons.put(0, basic); grid.add(basic);

        // Dynamic maps from memory
        List<Integer> ids = MapManager.getMapIds();
        for (int id : ids) {
            JToggleButton btn = new JToggleButton("Map " + id);
            styleButton(btn);
            boolean unlocked = GameSettings.isDeveloperModeEnabled()
                    || ProgressManager.isMapUnlocked(id);
            if (!unlocked) {
                btn.setText("🔒 Map " + id);
                btn.setEnabled(false);
            }
            final int m = id;
            btn.addActionListener(e -> selectedMap = m);
            group.add(btn);
            mapButtons.put(id, btn);
            grid.add(btn);
        }

        grid.revalidate();
        grid.repaint();
    }

    private void styleButton(AbstractButton b) {
        b.setFont(new Font("Arial", Font.PLAIN, 16));
        b.setForeground(Color.WHITE);
        b.setBackground(Color.DARK_GRAY);
        b.setFocusPainted(false);
    }
}
