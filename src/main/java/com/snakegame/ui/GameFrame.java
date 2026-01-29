package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameSnapshot;
import com.snakegame.ai.AiMode;
import com.snakegame.sound.BackgroundMusicPlayer;
import com.snakegame.sound.MusicManager;
import com.snakegame.ui.flow.GameFrameMenuController;
import com.snakegame.ui.flow.MenuFlowApplier;
import com.snakegame.ui.flow.MenuFlowDecider;
import com.snakegame.util.GameSaveManager;
import com.snakegame.util.ProgressManager;
import com.snakegame.view.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window that hosts the game and menus using a {@link CardLayout}.
 *
 * <p>Switches between panels such as the main menu, gameplay, settings, stats, and replay views.</p>
 */
public class GameFrame extends JFrame implements GameFrameMenuController.Host {

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private GamePanel gamePanel;
    private MainMenuPanel menuPanel;
    private ReplayPanel replayPanel;
    private final GameFrameMenuController menuController;

    /**
     * Creates and displays the main game window.
     */
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
        this.menuController = new GameFrameMenuController(this);

        // Panels
        this.menuPanel = new MainMenuPanel(this::handleMenuAction);
        recreateGamePanel();

        SettingsPanel settingsPanel = new SettingsPanel(e -> cardLayout.show(cardPanel, "menu"));
        StatsPanel statsPanel = new StatsPanel(e -> cardLayout.show(cardPanel, "menu"));
         replayPanel = new ReplayPanel(() -> {
            cardLayout.show(cardPanel, "menu");
            MusicManager.update(MusicManager.Screen.MAIN_MENU);
        });

        // Add cards
        cardPanel.add(this.menuPanel, "menu");
        cardPanel.add(replayPanel, "replay");
        cardPanel.add(settingsPanel, "settings");
        cardPanel.add(statsPanel, "stats");
        cardPanel.putClientProperty("menu", this.menuPanel);
        cardPanel.putClientProperty("replay", replayPanel);
        cardPanel.putClientProperty("settings", settingsPanel);
        cardPanel.putClientProperty("stats", statsPanel);

        this.add(cardPanel);
        this.pack();
        setFrameToBoardSize();          // sets frame to board size
        cardLayout.show(cardPanel, "menu"); // then show menu inside same frame
        this.setLocationRelativeTo(null);
        java.net.URL iconUrl = GameFrame.class.getResource("/snake_icon.png");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            this.setIconImage(icon.getImage());
        } else {
            // Fallback for IDE runs where resources may not be on the classpath.
            ImageIcon icon = new ImageIcon("resources/snake_icon.png");
            this.setIconImage(icon.getImage());
        }
        this.setVisible(true);
        MusicManager.update(MusicManager.Screen.MAIN_MENU);
        gamePanel.requestFocusInWindow();

    }

    private void handleMenuAction(ActionEvent e) {
        menuController.handleActionCommand(e.getActionCommand());
    }

    @Override
    public MenuFlowDecider.MenuState menuState() {
        return new MenuFlowDecider.MenuState(
                GameSettings.getCurrentMode(),
                GameSettings.getSelectedMapId(),
                GameSettings.isDeveloperModeEnabled()
        );
    }

    @Override
    public void applyStartDecision(MenuFlowDecider.StartDecision decision) {
        MenuFlowApplier.applyStartDecision(decision);
    }

    @Override
    public void startNewGame() {
        recreateGamePanel();
        showGameCardExact();
        gamePanel.startGame();
        MusicManager.update(MusicManager.Screen.GAMEPLAY);
    }

    @Override
    public void startGameFromSnapshot(GameSnapshot snapshot) {
        recreateGamePanel(snapshot);
        showGameCardExact();
        gamePanel.startGame();
    }

    @Override
    public void replaceCardPanel(String cardName, JPanel panel) {
        replaceCard(cardName, panel);
    }

    @Override
    public void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }

    @Override
    public void onReplayShow() {
        replayPanel.onShow();
    }

    @Override
    public void updateMusic(MusicManager.Screen screen) {
        MusicManager.update(screen);
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void confirmAndExit() {
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

    @Override
    public void setAiMode(AiMode mode) {
        GameSettings.setAiMode(mode);
    }

    @Override
    public java.util.Optional<GameSaveManager.ContinueSession> beginContinue() {
        return ProgressManager.beginContinue();
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
        cardPanel.putClientProperty("game", gamePanel);
    }

    private void replaceCard(String cardName, JPanel newPanel) {
        Object existing = cardPanel.getClientProperty(cardName);
        if (existing instanceof Component old) {
            cardPanel.remove(old);
        }
        cardPanel.add(newPanel, cardName);
        cardPanel.putClientProperty(cardName, newPanel);
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
