package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.model.GameSnapshot;
import com.snakegame.testutil.SettingsGuard;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.util.AppPaths;
import com.snakegame.util.GameSaveManager;
import com.snakegame.util.ProgressManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuPanelSmokeTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetSavePath() {
        GameSaveManager.setFilePath(AppPaths.SAVE_FILE.toString());
        ProgressManager.setFilePath(AppPaths.PROGRESS_FILE.toString());
        ProgressManager.load();
    }

    @Test
    void clickingMenuButtons_emitsExpectedActionCommands() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setDeveloperModeEnabled(false);
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            AtomicReference<List<String>> received = new AtomicReference<>(new ArrayList<>());
            MainMenuPanel[] panel = new MainMenuPanel[1];
            SwingUtilities.invokeAndWait(() -> panel[0] = new MainMenuPanel(evt -> received.get().add(evt.getActionCommand())));

            click(panel[0], "play");
            click(panel[0], "race");
            click(panel[0], "aiMenu");
            click(panel[0], "mode");
            click(panel[0], "settings");

            assertEquals(List.of("play", "race", "aiMenu", "mode", "settings"), received.get());
        }
    }

    @Test
    void continueButton_hiddenWhenNoSaveExists() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setDeveloperModeEnabled(false);
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            MainMenuPanel[] panel = new MainMenuPanel[1];
            SwingUtilities.invokeAndWait(() -> panel[0] = new MainMenuPanel(evt -> { }));

            AbstractButton cont = findButton(panel[0], b -> "continue".equals(b.getActionCommand()));
            assertFalse(cont.isVisible());
        }
    }

    @Test
    void continueButton_showsSavedScore() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setDeveloperModeEnabled(false);
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            // Create a minimal save with a known score.
            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.setCurrentMode(GameMode.STANDARD);
                GameSettings.setSelectedMapId(1);
            });
            GameSnapshot snap = new GameSnapshot();
            snap.settingsSnapshot = GameSettings.snapshot();
            snap.mode = GameMode.STANDARD;
            snap.selectedMapId = 1;
            snap.score = 12;
            GameSaveManager.save(snap);

            MainMenuPanel[] panel = new MainMenuPanel[1];
            SwingUtilities.invokeAndWait(() -> panel[0] = new MainMenuPanel(evt -> { }));

            AbstractButton cont = findButton(panel[0], b -> "continue".equals(b.getActionCommand()));
            assertTrue(cont.isVisible());
            assertTrue(cont.getText().contains("Continue"));
            assertTrue(cont.getText().contains("12"));
        }
    }

    private static void click(Container root, String actionCommand) throws Exception {
        AbstractButton b = findButton(root, btn -> actionCommand.equals(btn.getActionCommand()));
        SwingUtilities.invokeAndWait(b::doClick);
    }

    private static AbstractButton findButton(Container root, Predicate<AbstractButton> predicate) {
        for (AbstractButton b : allButtons(root)) {
            if (predicate.test(b)) return b;
        }
        fail("Button not found");
        return null;
    }

    private static List<AbstractButton> allButtons(Container root) {
        List<AbstractButton> out = new ArrayList<>();
        collectButtons(root, out);
        return out;
    }

    private static void collectButtons(Component c, List<AbstractButton> out) {
        if (c instanceof AbstractButton b) out.add(b);
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                collectButtons(child, out);
            }
        }
    }
}

