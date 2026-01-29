package com.snakegame.ui.flow;

import com.snakegame.ai.AiMode;
import com.snakegame.model.GameSnapshot;
import com.snakegame.sound.MusicManager;
import com.snakegame.ui.AiModePanel;
import com.snakegame.ui.DifficultyPanel;
import com.snakegame.ui.LeaderboardPanel;
import com.snakegame.ui.MapEditorPanel;
import com.snakegame.ui.MapModePanel;
import com.snakegame.ui.SettingsPanel;
import com.snakegame.ui.StatsPanel;
import com.snakegame.util.GameSaveManager;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinator for main-menu actions.
 *
 * <p>Routes action commands via {@link MenuRouter} and executes them against a host abstraction so
 * Swing wiring stays in {@code GameFrame} while decision logic remains testable.</p>
 */
public final class GameFrameMenuController {

    /**
     * Host interface implemented by the main window.
     *
     * <p>Methods are intentionally small primitives so the controller owns the menu flow logic.</p>
     */
    public interface Host {
        MenuFlowDecider.MenuState menuState();

        void applyStartDecision(MenuFlowDecider.StartDecision decision);

        void startNewGame();

        void startGameFromSnapshot(GameSnapshot snapshot);

        void replaceCardPanel(String cardName, JPanel panel);

        void showCard(String cardName);

        void onReplayShow();

        void updateMusic(MusicManager.Screen screen);

        void showMessage(String message);

        void confirmAndExit();

        void setAiMode(AiMode mode);

        Optional<GameSaveManager.ContinueSession> beginContinue();
    }

    private final Host host;

    public GameFrameMenuController(Host host) {
        this.host = Objects.requireNonNull(host, "host");
    }

    public void handleActionCommand(String actionCommand) {
        MenuCommand cmd = MenuRouter.route(actionCommand, host.menuState());

        if (cmd instanceof MenuCommand.StartRun start) {
            host.applyStartDecision(start.decision());
            host.startNewGame();
            return;
        }

        if (cmd instanceof MenuCommand.Navigate nav) {
            handleNavigate(nav.destination());
            return;
        }

        if (cmd instanceof MenuCommand.Continue) {
            handleContinue();
            return;
        }

        if (cmd instanceof MenuCommand.Exit) {
            host.confirmAndExit();
        }
    }

    private void handleNavigate(MenuCommand.Destination destination) {
        switch (destination) {
            case AI_MENU -> {
                AiModePanel aiPanel = new AiModePanel(
                        () -> host.showCard("menu"),
                        this::handleAiStart
                );
                host.replaceCardPanel("aiMode", aiPanel);
                host.showCard("aiMode");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case MODE -> {
                MapModePanel modePanel = new MapModePanel(() -> host.showCard("menu"));
                host.replaceCardPanel("mode", modePanel);
                host.showCard("mode");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case REPLAY -> {
                host.onReplayShow();
                host.showCard("replay");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case DIFFICULTY -> {
                JPanel diffPanel = new DifficultyPanel(() -> host.showCard("menu"));
                host.replaceCardPanel("difficulty", diffPanel);
                host.showCard("difficulty");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case LEADERBOARD -> {
                LeaderboardPanel lb = new LeaderboardPanel(() -> {
                    host.showCard("menu");
                    host.updateMusic(MusicManager.Screen.MAIN_MENU);
                });
                host.replaceCardPanel("leaderboard", lb);
                host.showCard("leaderboard");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case SETTINGS -> {
                SettingsPanel settingsPanel = new SettingsPanel(evt -> {
                    host.showCard("menu");
                    // re-evaluate menu music after toggling
                    host.updateMusic(MusicManager.Screen.MAIN_MENU);
                });
                host.replaceCardPanel("settings", settingsPanel);
                host.showCard("settings");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case STATS -> {
                StatsPanel statsPanel = new StatsPanel(evt -> host.showCard("menu"));
                host.replaceCardPanel("stats", statsPanel);
                host.showCard("stats");
                host.updateMusic(MusicManager.Screen.MAIN_MENU);
            }
            case DEVELOPER -> {
                MapEditorPanel editor = new MapEditorPanel(() -> host.showCard("menu"));
                host.replaceCardPanel("mapEditor", editor);
                host.showCard("mapEditor");
            }
        }
    }

    private void handleAiStart(AiMode selectedAiMode) {
        host.setAiMode(selectedAiMode);
        host.applyStartDecision(MenuFlowDecider.decideAi(host.menuState()));
        host.startNewGame();
    }

    private void handleContinue() {
        Optional<GameSaveManager.ContinueSession> opt = host.beginContinue();
        if (opt.isEmpty()) {
            host.showMessage("No saved game found.");
            return;
        }

        try (GameSaveManager.ContinueSession session = opt.get()) {
            host.startGameFromSnapshot(session.snapshot());
            session.commit();
            host.updateMusic(MusicManager.Screen.GAMEPLAY);
        }
    }
}

