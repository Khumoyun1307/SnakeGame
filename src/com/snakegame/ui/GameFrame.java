package com.snakegame.ui;

import com.snakegame.sound.BackgroundMusicPlayer;
import com.snakegame.view.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private GamePanel gamePanel;


    public GameFrame() {
        this.setTitle("Snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Panels
        MainMenuPanel menuPanel = new MainMenuPanel(this::handleMenuAction);
        recreateGamePanel();
        gamePanel.addPropertyChangeListener("goToMenu", evt -> {
            cardLayout.show(cardPanel, "menu");
        });
        SettingsPanel settingsPanel = new SettingsPanel(e -> cardLayout.show(cardPanel, "menu"));
        StatsPanel statsPanel = new StatsPanel(e -> cardLayout.show(cardPanel, "menu"));


        // Add cards
        cardPanel.add(menuPanel, "menu");
        cardPanel.add(gamePanel, "game");
        cardPanel.add(settingsPanel, "settings");
        cardPanel.add(statsPanel, "stats");

        this.add(cardPanel);
        this.pack();
        this.setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon("resources/snake_icon.png"); // your icon path
        this.setIconImage(icon.getImage());
        this.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    private void handleMenuAction(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "play" -> {
                // BackgroundMusicPlayer.play("backgroundMusic.wav", true);
                recreateGamePanel();
                cardLayout.show(cardPanel, "game");
                gamePanel.startGame();
            }
            case "stats" -> {
                StatsPanel statsPanel = new StatsPanel(evt -> cardLayout.show(cardPanel, "menu"));
                replaceCard("stats", statsPanel);
                cardLayout.show(cardPanel, "stats");
            }
            case "settings" -> {
                SettingsPanel settingsPanel = new SettingsPanel(evt -> cardLayout.show(cardPanel, "menu"));
                replaceCard("settings", settingsPanel);
                cardLayout.show(cardPanel, "settings");
            }
            case "difficulty" -> {
                JPanel diffPanel = new DifficultyPanel(() -> cardLayout.show(cardPanel, "menu"));
                cardPanel.add(diffPanel, "difficulty");
                cardLayout.show(cardPanel, "difficulty");
            }
            case "mode" -> {
                JPanel modePanel = new ModePanel(() -> cardLayout.show(cardPanel, "menu"));
                cardPanel.add(modePanel, "mode");
                cardLayout.show(cardPanel, "mode");
            }
            case "exit" -> System.exit(0);
        }
    }

    private void recreateGamePanel() {
        if (gamePanel != null) {
            cardPanel.remove(gamePanel);
        }

        gamePanel = new GamePanel();

        // Listen for "goToMenu" again
        gamePanel.addPropertyChangeListener("goToMenu", evt -> {
            cardLayout.show(cardPanel, "menu");
        });

        cardPanel.add(gamePanel, "game");
    }

    private void replaceCard(String cardName, JPanel newPanel) {
        Component[] components = cardPanel.getComponents();
        for (Component c : components) {
            if (cardPanel.getClientProperty(cardName) == c || cardName.equals(cardPanel.getName())) {
                cardPanel.remove(c);
                break;
            }
        }
        cardPanel.add(newPanel, cardName);
    }

}
