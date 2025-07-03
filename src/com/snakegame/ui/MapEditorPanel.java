package com.snakegame.ui;

import com.snakegame.model.GameConfig;
import com.snakegame.mode.MapManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MapEditorPanel extends JPanel {
    private final int cols = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
    private final int rows = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;
    private final Cell[][] grid;

    public MapEditorPanel(Runnable goBack) {
        setLayout(new BorderLayout());
        // Grid canvas
        JPanel canvas = new JPanel(new GridLayout(rows, cols));
        grid = new Cell[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Cell cell = new Cell(x, y);
                canvas.add(cell);
                grid[y][x] = cell;
            }
        }

        // Control bar
        JPanel bar = new JPanel(new FlowLayout());
        JButton newBtn = new JButton("New");
        JButton saveBtn = new JButton("Save");
        JButton backBtn = new JButton("Back");

        newBtn.addActionListener(e -> clearGrid());
        saveBtn.addActionListener(e -> saveMap());
        backBtn.addActionListener(e -> goBack.run());

        bar.add(newBtn);
        bar.add(saveBtn);
        bar.add(backBtn);

        add(canvas, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);
    }

    private void clearGrid() {
        for (Cell[] row : grid)
            for (Cell c : row) c.setObstacle(false);
    }

    private void saveMap() {
        String input = JOptionPane.showInputDialog(
                this,
                "Enter new map ID (numeric):",
                "Save Map",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            int id = Integer.parseInt(input.trim());
            List<Point> obs = new ArrayList<>();
            for (Cell[] row : grid) {
                for (Cell c : row) {
                    if (c.isObstacle()) {
                        obs.add(new Point(c.x * GameConfig.UNIT_SIZE,
                                c.y * GameConfig.UNIT_SIZE));
                    }
                }
            }
            MapManager.saveMapConfig(id, obs);
            JOptionPane.showMessageDialog(this, "Map " + id + " saved successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID.");
        }
    }

    // Single grid cell
    private class Cell extends JPanel {
        final int x, y;
        private boolean obstacle = false;
        Cell(int x, int y) {
            this.x = x;
            this.y = y;
            setPreferredSize(new Dimension(GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE));
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setObstacle(!obstacle);
                }
            });
        }
        void setObstacle(boolean obs) {
            obstacle = obs;
            setBackground(obs ? Color.GRAY : Color.BLACK);
            repaint();
        }
        boolean isObstacle() {
            return obstacle;
        }
    }
}