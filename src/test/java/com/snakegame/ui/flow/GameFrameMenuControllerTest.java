package com.snakegame.ui.flow;

import com.snakegame.ai.AiMode;
import com.snakegame.model.GameSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.sound.MusicManager;
import com.snakegame.testutil.SettingsGuard;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.ui.AiModePanel;
import com.snakegame.ui.MapEditorPanel;
import com.snakegame.ui.MapModePanel;
import com.snakegame.util.AppPaths;
import com.snakegame.util.GameSaveManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameFrameMenuControllerTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetSavePath() {
        GameSaveManager.setFilePath(AppPaths.SAVE_FILE.toString());
    }

    @Test
    void play_appliesDecisionThenStartsNewGame() {
        RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        GameFrameMenuController controller = new GameFrameMenuController(host);

        controller.handleActionCommand("play");

        assertEquals(List.of("menuState", "applyStartDecision", "startNewGame"), host.calls);
    }

    @Test
    void aiMenu_showsAiPanelCardAndUpdatesMenuMusic() {
        RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        GameFrameMenuController controller = new GameFrameMenuController(host);

        controller.handleActionCommand("aiMenu");

        assertEquals("aiMode", host.lastReplacedCard);
        assertInstanceOf(AiModePanel.class, host.lastReplacedPanel);
        assertEquals("aiMode", host.lastShownCard);
        assertEquals(MusicManager.Screen.MAIN_MENU, host.lastMusicScreen);
        assertTrue(host.calls.contains("updateMusic"));
    }

    @Test
    void mode_showsMapModeCardAndUpdatesMenuMusic() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
            GameFrameMenuController controller = new GameFrameMenuController(host);

            controller.handleActionCommand("mode");

            assertEquals("mode", host.lastReplacedCard);
            assertInstanceOf(MapModePanel.class, host.lastReplacedPanel);
            assertEquals("mode", host.lastShownCard);
            assertEquals(MusicManager.Screen.MAIN_MENU, host.lastMusicScreen);
        }
    }

    @Test
    void replay_callsOnShowAndUpdatesMenuMusic() {
        RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        GameFrameMenuController controller = new GameFrameMenuController(host);

        controller.handleActionCommand("replay");

        assertEquals(List.of("menuState", "onReplayShow", "showCard", "updateMusic"), host.calls);
        assertEquals("replay", host.lastShownCard);
        assertEquals(MusicManager.Screen.MAIN_MENU, host.lastMusicScreen);
    }

    @Test
    void developer_showsEditorCardWithoutUpdatingMusic() {
        RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        GameFrameMenuController controller = new GameFrameMenuController(host);

        controller.handleActionCommand("developer");

        assertEquals("mapEditor", host.lastReplacedCard);
        assertInstanceOf(MapEditorPanel.class, host.lastReplacedPanel);
        assertEquals("mapEditor", host.lastShownCard);
        assertFalse(host.calls.contains("updateMusic"), "Developer screen should not trigger menu music update");
    }

    @Test
    void continue_whenNoSave_showsMessage() {
        RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        host.continueBehavior = ContinueBehavior.EMPTY;
        GameFrameMenuController controller = new GameFrameMenuController(host);

        controller.handleActionCommand("continue");

        assertEquals(List.of("menuState", "beginContinue", "showMessage"), host.calls);
        assertEquals("No saved game found.", host.lastMessage);
    }

    @Test
    void continue_whenSaveExists_startsFromSnapshotCommitsAndUpdatesGameplayMusic() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path save = tmp.resolve("savegame.txt");
            Path resume = tmp.resolve("savegame.txt.resume");
            GameSaveManager.setFilePath(save.toString());
            if (Files.exists(save)) Files.delete(save);
            if (Files.exists(resume)) Files.delete(resume);

            GameSnapshot snap = new GameSnapshot();
            snap.score = 7;
            snap.snakeBody = List.of(new java.awt.Point(40, 40));
            GameSaveManager.save(snap);
            assertTrue(Files.exists(save));

            RecordingHost host = new RecordingHost(new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
            host.continueBehavior = ContinueBehavior.REAL_SESSION;
            GameFrameMenuController controller = new GameFrameMenuController(host);

            controller.handleActionCommand("continue");

            assertFalse(Files.exists(save), "Continue should move/delete the save file after a successful start");
            assertFalse(Files.exists(resume), "Continue commit should delete the resume file");

            assertTrue(host.calls.contains("startGameFromSnapshot"));
            assertEquals(MusicManager.Screen.GAMEPLAY, host.lastMusicScreen);
        }
    }

    private enum ContinueBehavior { EMPTY, REAL_SESSION }

    private static final class RecordingHost implements GameFrameMenuController.Host {
        private final MenuFlowDecider.MenuState state;
        private final List<String> calls = new ArrayList<>();

        private MenuFlowDecider.StartDecision lastDecision;
        private String lastReplacedCard;
        private JPanel lastReplacedPanel;
        private String lastShownCard;
        private MusicManager.Screen lastMusicScreen;
        private String lastMessage;
        private AiMode lastAiMode;

        private ContinueBehavior continueBehavior = ContinueBehavior.EMPTY;

        private RecordingHost(MenuFlowDecider.MenuState state) {
            this.state = state;
        }

        @Override
        public MenuFlowDecider.MenuState menuState() {
            calls.add("menuState");
            return state;
        }

        @Override
        public void applyStartDecision(MenuFlowDecider.StartDecision decision) {
            calls.add("applyStartDecision");
            lastDecision = decision;
        }

        @Override
        public void startNewGame() {
            calls.add("startNewGame");
        }

        @Override
        public void startGameFromSnapshot(GameSnapshot snapshot) {
            calls.add("startGameFromSnapshot");
        }

        @Override
        public void replaceCardPanel(String cardName, JPanel panel) {
            calls.add("replaceCardPanel");
            lastReplacedCard = cardName;
            lastReplacedPanel = panel;
        }

        @Override
        public void showCard(String cardName) {
            calls.add("showCard");
            lastShownCard = cardName;
        }

        @Override
        public void onReplayShow() {
            calls.add("onReplayShow");
        }

        @Override
        public void updateMusic(MusicManager.Screen screen) {
            calls.add("updateMusic");
            lastMusicScreen = screen;
        }

        @Override
        public void showMessage(String message) {
            calls.add("showMessage");
            lastMessage = message;
        }

        @Override
        public void confirmAndExit() {
            calls.add("confirmAndExit");
        }

        @Override
        public void setAiMode(AiMode mode) {
            calls.add("setAiMode");
            lastAiMode = mode;
        }

        @Override
        public Optional<GameSaveManager.ContinueSession> beginContinue() {
            calls.add("beginContinue");
            return switch (continueBehavior) {
                case EMPTY -> Optional.empty();
                case REAL_SESSION -> GameSaveManager.beginContinue();
            };
        }
    }
}

