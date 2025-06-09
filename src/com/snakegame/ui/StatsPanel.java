package com.snakegame.ui;

import com.snakegame.util.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class StatsPanel extends JPanel {
    private static final int PAGE_SIZE = 10;

    private int currentPage = 0;
    private boolean showHistoryMode = false;

    private final List<String> rawScores;
    private final JPanel scoreListPanel;
    private final JButton nextButton;
    private final JButton prevButton;
    private final JButton toggleViewButton;
    private final JLabel highScoreLabel;

    public StatsPanel(ActionListener onBack) {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);

        rawScores = ScoreManager.getScores();

        // Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.BLACK);

        JLabel title = new JLabel("🏆 SCOREBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Ink Free", Font.BOLD, 40));
        title.setForeground(Color.ORANGE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        titlePanel.add(title, BorderLayout.NORTH);

        highScoreLabel = new JLabel("", SwingConstants.CENTER);
        highScoreLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        highScoreLabel.setForeground(Color.YELLOW);
        highScoreLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));
        titlePanel.add(highScoreLabel, BorderLayout.SOUTH);

        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.ORANGE);
        separator.setBackground(Color.ORANGE);
        separator.setPreferredSize(new Dimension(1, 2));

        JPanel separatorWrapper = new JPanel(new BorderLayout());
        separatorWrapper.setBackground(Color.BLACK);
        separatorWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        separatorWrapper.add(separator, BorderLayout.CENTER);
        titlePanel.add(separatorWrapper, BorderLayout.CENTER);

        this.add(titlePanel, BorderLayout.NORTH);

        // Score list area
        scoreListPanel = new JPanel();
        scoreListPanel.setLayout(new BoxLayout(scoreListPanel, BoxLayout.Y_AXIS));
        scoreListPanel.setBackground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(scoreListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        this.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        navPanel.setBackground(Color.BLACK);

        prevButton = new JButton("⏮ Previous");
        nextButton = new JButton("Next ⏭");
        toggleViewButton = new JButton("📝 Show Play History");
        JButton backButton = new JButton("⬅ Back to Menu");

        prevButton.addActionListener(e -> {
            currentPage--;
            updateScoreDisplay();
        });

        nextButton.addActionListener(e -> {
            currentPage++;
            updateScoreDisplay();
        });

        toggleViewButton.addActionListener(e -> {
            showHistoryMode = !showHistoryMode;
            currentPage = 0;
            toggleViewButton.setText(showHistoryMode ? "🔢 Show Scores Only" : "📝 Show Play History");
            updateScoreDisplay();
        });

        backButton.addActionListener(onBack);

        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(toggleViewButton);
        navPanel.add(backButton);

        this.add(navPanel, BorderLayout.SOUTH);

        updateScoreDisplay(); // initial render
    }

    private void updateScoreDisplay() {
        scoreListPanel.removeAll();

        List<String> displayList;

        if (showHistoryMode) {
            // Use full original list
            displayList = rawScores;
        } else {
            // Extract only numbers and sort high-to-low
            displayList = rawScores.stream()
                    .map(line -> {
                        try {
                            String[] parts = line.split("Score: ");
                            return Integer.parseInt(parts[1].trim());
                        } catch (Exception e) {
                            return 0;
                        }
                    }).distinct()
                    .sorted(Comparator.reverseOrder())
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }

        int total = displayList.size();
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);

        if (total == 0) {
            JLabel empty = new JLabel("No scores recorded yet.", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            empty.setFont(new Font("Consolas", Font.ITALIC, 18));
            scoreListPanel.add(empty);
        } else {
            for (int i = start; i < end; i++) {
                String labelText = (i + 1) + ".  " + displayList.get(i);
                JLabel label = new JLabel(labelText);
                label.setFont(new Font("Consolas", Font.BOLD, 20));
                label.setForeground(Color.GREEN);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                scoreListPanel.add(label);
            }
        }

        highScoreLabel.setText("🔥 High Score: " + ScoreManager.getHighScore());

        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(end < displayList.size());

        revalidate();
        repaint();
    }
}
