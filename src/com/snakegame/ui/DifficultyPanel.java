package com.snakegame.ui;

import com.snakegame.config.GameSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import javax.swing.event.ChangeEvent;

public class DifficultyPanel extends JPanel {
    private static final int MIN_DIFFICULTY = 0;
    private static final int MAX_DIFFICULTY = 50;

    private static final int BEGINNER_LEVEL = 0;
    private static final int EASY_LEVEL = 10;
    private static final int MEDIUM_LEVEL = 20;
    private static final int HARD_LEVEL = 30;
    private static final int EXPERT_LEVEL = 40;
    private static final int INSANE_LEVEL = 50;

    private final JSlider speedSlider;
    private final JLabel speedLabel;

    public DifficultyPanel(Runnable goBack) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Difficulty Settings");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        // Slider between Easy and Insane
        speedSlider = new JSlider(MIN_DIFFICULTY, MAX_DIFFICULTY, GameSettings.getDifficultyLevel());
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setBackground(Color.BLACK);
        speedSlider.setForeground(Color.GREEN);
        speedSlider.setLabelTable(createSliderLabels());


        // Fancy label for difficulty
        speedLabel = new JLabel();
        speedLabel.setAlignmentX(CENTER_ALIGNMENT);
        speedLabel.setForeground(Color.CYAN);
        speedLabel.setFont(new Font("Consolas", Font.PLAIN, 18));
        updateSpeedLabel(speedSlider.getValue());

        speedSlider.addChangeListener((ChangeEvent e) -> {
            updateSpeedLabel(speedSlider.getValue());
        });

        JCheckBox obstacleCheckbox = new JCheckBox("Enable Random Obstacles");
        obstacleCheckbox.setSelected(GameSettings.isObstaclesEnabled());
        obstacleCheckbox.setForeground(Color.WHITE);
        obstacleCheckbox.setBackground(Color.BLACK);
        obstacleCheckbox.setAlignmentX(CENTER_ALIGNMENT);

        JButton save = new JButton("✅ Save");
        save.setAlignmentX(CENTER_ALIGNMENT);
        save.addActionListener(e -> {
            GameSettings.setDifficultyLevel(speedSlider.getValue());
            GameSettings.setObstaclesEnabled(obstacleCheckbox.isSelected());
            JOptionPane.showMessageDialog(this, "Settings saved!");
        });


        JButton back = new JButton("← Back");
        back.setAlignmentX(CENTER_ALIGNMENT);
        back.addActionListener(e -> goBack.run());

        add(title);
        add(Box.createVerticalStrut(30));
        add(speedSlider);
        add(Box.createVerticalStrut(10));
        add(speedLabel);
        add(Box.createVerticalStrut(20));
        add(obstacleCheckbox);
        add(Box.createVerticalStrut(20));
        add(save);
        add(Box.createVerticalStrut(10));
        add(back);
    }

    private void updateSpeedLabel(int level) {
        String label;
        if (level < 10) label = "Difficulty: Beginner";
        else if (level < 20) label = "Difficulty: Easy";
        else if (level < 30) label = "Difficulty: Medium";
        else if (level < 40) label = "Difficulty: Hard";
        else if (level < 50) label = "Difficulty: Expert";
        else label = "Difficulty: Insane";

        speedLabel.setText(label + " (Level " + level + ")");
    }


    private Hashtable<Integer, JLabel> createSliderLabels() {
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(BEGINNER_LEVEL, createTickLabel("Beginner"));
        labels.put(EASY_LEVEL, createTickLabel("Easy"));
        labels.put(MEDIUM_LEVEL, createTickLabel("Medium"));
        labels.put(HARD_LEVEL, createTickLabel("Hard"));
        labels.put(EXPERT_LEVEL, createTickLabel("Expert"));
        labels.put(INSANE_LEVEL, createTickLabel("Insane"));
        return labels;
    }


    private JLabel createTickLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }

    private int getDelayFromDifficulty(GameSettings.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> EASY_LEVEL;
            case NORMAL -> MEDIUM_LEVEL;
            case HARD -> HARD_LEVEL;
            case EXPERT -> EXPERT_LEVEL;
            case INSANE -> INSANE_LEVEL;
        };
    }
}
