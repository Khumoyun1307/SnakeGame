package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.sound.BackgroundMusicPlayer;
import com.snakegame.sound.MusicManager;
import com.snakegame.view.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private GamePanel gamePanel;


    public GameFrame() {
        this.setTitle("Snake");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                GameFrame.this,
                            "Are you sure you want to quit the game?",
                            "Confirm Exit",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        this.setResizable(false);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Panels
        MainMenuPanel menuPanel = new MainMenuPanel(this::handleMenuAction);
        recreateGamePanel();
        gamePanel.addPropertyChangeListener("goToMenu", evt -> {
            cardLayout.show(cardPanel, "menu");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
        });
        gamePanel.addPropertyChangeListener("showSettings", evt -> {
            BackgroundMusicPlayer.stop();      // optional: stop music
            cardLayout.show(cardPanel, "settings");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
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
        MusicManager.update(MusicManager.Screen.MAIN_MENU);
        gamePanel.requestFocusInWindow();

    }

    private void handleMenuAction(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "play" -> {
                recreateGamePanel();
                cardLayout.show(cardPanel, "game");
                gamePanel.startGame();

                MusicManager.update(MusicManager.Screen.GAMEPLAY);
            }
            case "race" -> {
                GameSettings.setCurrentMode(GameMode.RACE);
                GameSettings.setSelectedMapId(1);
                recreateGamePanel();
                cardLayout.show(cardPanel, "game");
                gamePanel.startGame();

                MusicManager.update(MusicManager.Screen.GAMEPLAY);
            }
            case "mode" -> {
                ModePanel modePanel = new ModePanel(() -> cardLayout.show(cardPanel, "menu"));
                replaceCard("mode", modePanel);
                cardLayout.show(cardPanel, "mode");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "difficulty" -> {
                JPanel diffPanel = new DifficultyPanel(() -> cardLayout.show(cardPanel, "menu"));
                replaceCard("difficulty", diffPanel);
                cardLayout.show(cardPanel, "difficulty");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "settings" -> {
                SettingsPanel settingsPanel = new SettingsPanel(evt ->
                {
                    cardLayout.show(cardPanel, "menu");
                    // re-evaluate menu music after toggling
                    MusicManager.update(MusicManager.Screen.MAIN_MENU);
                });

                replaceCard("settings", settingsPanel);
                cardLayout.show(cardPanel, "settings");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "stats" -> {
                StatsPanel statsPanel = new StatsPanel(evt -> cardLayout.show(cardPanel, "menu"));
                replaceCard("stats", statsPanel);
                cardLayout.show(cardPanel, "stats");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "exit" -> {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to quit the game?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
            case "developer" -> {
                MapEditorPanel editor = new MapEditorPanel(() -> cardLayout.show(cardPanel, "menu"));
                cardPanel.add(editor, "mapEditor");
                cardLayout.show(cardPanel, "mapEditor");
            }

        }
    }

    private void recreateGamePanel() {
        if (gamePanel != null) {
            cardPanel.remove(gamePanel);
        }

        gamePanel = new GamePanel();

        // Listen for "goToMenu" again
        gamePanel.addPropertyChangeListener("goToMenu", evt -> {
            BackgroundMusicPlayer.stop();
            cardLayout.show(cardPanel, "menu");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
        });

        // ALSO stop music and show settings
        gamePanel.addPropertyChangeListener("showSettings", evt -> {
            BackgroundMusicPlayer.stop();
            cardLayout.show(cardPanel, "settings");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
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
