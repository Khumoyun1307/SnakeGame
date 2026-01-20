package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameSnapshot;
import com.snakegame.sound.BackgroundMusicPlayer;
import com.snakegame.sound.MusicManager;
import com.snakegame.util.ProgressManager;
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
    private MainMenuPanel menuPanel;
    private ReplayPanel replayPanel;

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
        this.menuPanel = new MainMenuPanel(this::handleMenuAction);
        recreateGamePanel();
        gamePanel.addPropertyChangeListener("goToMenu", evt -> {
            cardLayout.show(cardPanel, "menu");
            this.menuPanel.refreshContinueButton(); // in-session update
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
        });
        gamePanel.addPropertyChangeListener("showSettings", evt -> {
            BackgroundMusicPlayer.stop();      // optional: stop music
            cardLayout.show(cardPanel, "settings");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
        });

        SettingsPanel settingsPanel = new SettingsPanel(e -> cardLayout.show(cardPanel, "menu"));
        StatsPanel statsPanel = new StatsPanel(e -> cardLayout.show(cardPanel, "menu"));
         replayPanel = new ReplayPanel(() -> {
            cardLayout.show(cardPanel, "menu");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
        });

        // Add cards
        cardPanel.add(this.menuPanel, "menu");
        cardPanel.add(gamePanel, "game");
        cardPanel.add(replayPanel, "replay");
        cardPanel.add(settingsPanel, "settings");
        cardPanel.add(statsPanel, "stats");

        this.add(cardPanel);
        this.pack();
        setFrameToBoardSize();          // sets frame to board size
        cardLayout.show(cardPanel, "menu"); // then show menu inside same frame
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
                if (GameSettings.getCurrentMode() == GameMode.AI || GameSettings.getCurrentMode() == GameMode.RACE){
                    int mapId = GameSettings.getSelectedMapId();
                    if (mapId >= 1) {
                        GameSettings.setCurrentMode(GameMode.MAP_SELECT);
                    } else {
                        GameSettings.setCurrentMode(GameMode.STANDARD);
                    }
                }

                ProgressManager.clearSavedGame();
                recreateGamePanel();
                showGameCardExact();
                gamePanel.startGame();

                MusicManager.update(MusicManager.Screen.GAMEPLAY);
            }
            case "race" -> {
                GameSettings.setCurrentMode(GameMode.RACE);
                GameSettings.setSelectedMapId(1);
                recreateGamePanel();
                showGameCardExact();
                gamePanel.startGame();

                MusicManager.update(MusicManager.Screen.GAMEPLAY);
            }
            case "aiMenu" -> {
                AiModePanel aiPanel = new AiModePanel(
                        () -> cardLayout.show(cardPanel, "menu"),
                        (selectedAiMode) -> {
                            GameSettings.setAiMode(selectedAiMode);
                            GameSettings.setCurrentMode(GameMode.AI);

                            ProgressManager.clearSavedGame();
                            recreateGamePanel();
                            cardLayout.show(cardPanel, "game");
                            gamePanel.startGame();
                            MusicManager.update(MusicManager.Screen.GAMEPLAY);
                        }
                );
                replaceCard("aiMode", aiPanel);
                cardLayout.show(cardPanel, "aiMode");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }

            case "mode" -> {
                MapModePanel modePanel = new MapModePanel(() -> cardLayout.show(cardPanel, "menu"));
                replaceCard("mode", modePanel);
                cardLayout.show(cardPanel, "mode");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "replay" -> {
                replayPanel.onShow();
                cardLayout.show(cardPanel, "replay");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "difficulty" -> {
                JPanel diffPanel = new DifficultyPanel(() -> cardLayout.show(cardPanel, "menu"));
                replaceCard("difficulty", diffPanel);
                cardLayout.show(cardPanel, "difficulty");
                MusicManager.update(MusicManager.Screen.MAIN_MENU);
            }
            case "leaderboard" -> {
                LeaderboardPanel lb = new LeaderboardPanel(() -> {
                    cardLayout.show(cardPanel, "menu");
                    MusicManager.update(MusicManager.Screen.MAIN_MENU);
                });
                replaceCard("leaderboard", lb);
                cardLayout.show(cardPanel, "leaderboard");
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

            case "continue" -> {
                var opt = ProgressManager.loadGame();
                if (opt.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No saved game found.");
                    return;
                }
                ProgressManager.clearSavedGame();
                recreateGamePanel(opt.get());
                showGameCardExact();
                gamePanel.startGame();
                MusicManager.update(MusicManager.Screen.GAMEPLAY);
            }

        }
    }

    private void recreateGamePanel() { recreateGamePanel(null); }


    private void recreateGamePanel(GameSnapshot snapshot) {
        if (gamePanel != null) {
            cardPanel.remove(gamePanel);
        }
        gamePanel = (snapshot == null) ? new GamePanel() : new GamePanel(snapshot);

        // Listen for "goToMenu" again
        gamePanel.addPropertyChangeListener("goToMenu", evt -> {
            BackgroundMusicPlayer.stop();
            cardLayout.show(cardPanel, "menu");
            this.menuPanel.refreshContinueButton();
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

    private void setFrameToBoardSize() {
        Insets insets = getInsets();
        int frameW = GameConfig.SCREEN_WIDTH + insets.left + insets.right;
        int frameH = GameConfig.SCREEN_HEIGHT + insets.top + insets.bottom;

        setSize(frameW, frameH);
        setLocationRelativeTo(null);
    }

    private void showGameCardExact() {
        cardLayout.show(cardPanel, "game");
        cardPanel.revalidate();
        cardPanel.repaint();
        setFrameToBoardSize();
        cardPanel.doLayout();
        gamePanel.requestFocusInWindow();
    }

}
