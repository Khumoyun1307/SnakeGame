package com.snakegame.ui.flow;

import com.snakegame.config.GameSettings;
import com.snakegame.config.GameSettingsManager;
import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapManager;
import com.snakegame.model.GameState;
import com.snakegame.testutil.SettingsGuard;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.ui.MapModePanel;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scenario-style tests that mirror common manual UI flows without instantiating {@link com.snakegame.ui.GameFrame}.
 *
 * <p>These tests drive panel interactions and then apply the same start-decision logic used by the menu.</p>
 */
class ManualFlowScenariosTest extends SnakeTestBase {

    @TempDir
    Path tmp;

    @AfterEach
    void resetFilePaths() {
        GameSettingsManager.setFilePath(null);
        ProgressManager.setFilePath(AppPaths.PROGRESS_FILE.toString());
        ProgressManager.load();
        GameSaveManager.setFilePath(AppPaths.SAVE_FILE.toString());
    }

    @Test
    void mapSelect_thenPlay_loadsSelectedMapObstacles() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            // Fully isolate persistence used by this flow.
            GameSettingsManager.setFilePath(tmp.resolve("settings.properties").toString());
            ProgressManager.setFilePath(tmp.resolve("progress.txt").toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();
            ProgressManager.unlockMap(3);
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            GameSettings.setObstaclesEnabled(false);
            GameSettings.setMovingObstaclesEnabled(false);
            GameSettings.setCurrentMode(GameMode.STANDARD);
            GameSettings.setSelectedMapId(1);

            selectMapInPanelAndSave("Map 3");
            assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());

            apply(MenuFlowDecider.decidePlay(state()));
            assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());

            GameState s = new GameState(1L);
            assertEquals(GameMode.MAP_SELECT, s.getRunSettingsSnapshot().currentMode());
            assertEquals(3, s.getCurrentMapId());

            Set<Point> expected = new HashSet<>(MapManager.getMap(3).getObstacles());
            Set<Point> actual = new HashSet<>(s.getObstacles());
            assertEquals(expected, actual);
        }
    }

    @Test
    void mapSelect_thenAi_usesStandardObstaclesNotSelectedMap() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettingsManager.setFilePath(tmp.resolve("settings.properties").toString());
            ProgressManager.setFilePath(tmp.resolve("progress.txt").toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();
            ProgressManager.unlockMap(3);
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            GameSettings.setObstaclesEnabled(false);
            GameSettings.setMovingObstaclesEnabled(false);

            selectMapInPanelAndSave("Map 3");
            assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());

            apply(MenuFlowDecider.decideAi(state()));

            GameState s = new GameState(1L);
            assertEquals(GameMode.AI, s.getRunSettingsSnapshot().currentMode());
            assertTrue(s.getObstacles().isEmpty(), "AI runs should not inherit MAP_SELECT obstacles");
        }
    }

    @Test
    void basicMap_thenAi_doesNotUsePreviousMapSelection() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettingsManager.setFilePath(tmp.resolve("settings.properties").toString());
            ProgressManager.setFilePath(tmp.resolve("progress.txt").toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();
            ProgressManager.unlockMap(3);
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            GameSettings.setObstaclesEnabled(false);
            GameSettings.setMovingObstaclesEnabled(false);

            // User previously selected a specific map.
            selectMapInPanelAndSave("Map 3");
            assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());

            // User switches back to Basic Map.
            selectMapInPanelAndSave("Basic Map");
            assertEquals(GameMode.STANDARD, GameSettings.getCurrentMode());
            assertEquals(1, GameSettings.getSelectedMapId());

            apply(MenuFlowDecider.decideAi(state()));

            GameState s = new GameState(1L);
            assertEquals(GameMode.AI, s.getRunSettingsSnapshot().currentMode());
            assertTrue(s.getObstacles().isEmpty());
        }
    }

    @Test
    void race_ignoresPreviousMapSelectionAndStartsAtMap1() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            GameSettings.setCurrentMode(GameMode.MAP_SELECT);
            GameSettings.setSelectedMapId(7);

            apply(MenuFlowDecider.decideRace(state()));

            assertEquals(GameMode.RACE, GameSettings.getCurrentMode());
            assertEquals(1, GameSettings.getSelectedMapId());

            GameState s = new GameState(1L);
            assertEquals(1, s.getCurrentMapId());
        }
    }

    private MenuFlowDecider.MenuState state() {
        return new MenuFlowDecider.MenuState(
                GameSettings.getCurrentMode(),
                GameSettings.getSelectedMapId(),
                GameSettings.isDeveloperModeEnabled()
        );
    }

    private static void apply(MenuFlowDecider.StartDecision d) {
        if (d.modeToSet() != GameSettings.getCurrentMode()) {
            GameSettings.setCurrentMode(d.modeToSet());
        }
        if (d.selectedMapIdToSet() != null && d.selectedMapIdToSet() != GameSettings.getSelectedMapId()) {
            GameSettings.setSelectedMapId(d.selectedMapIdToSet());
        }
        if (d.clearSavedGameIfAllowed()) {
            ProgressManager.clearSavedGameIfAllowed(GameSettings.isDeveloperModeEnabled());
        }
    }

    private void selectMapInPanelAndSave(String buttonText) throws Exception {
        AtomicBoolean wentBack = new AtomicBoolean(false);
        MapModePanel[] panel = new MapModePanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new MapModePanel(() -> wentBack.set(true)));

        AbstractButton button = findButton(panel[0], b -> buttonText.equals(b.getText()));
        AbstractButton save = findButton(panel[0], b -> {
            String t = b.getText();
            return t != null && t.contains("Save");
        });

        SwingUtilities.invokeAndWait(button::doClick);
        SwingUtilities.invokeAndWait(save::doClick);
        assertTrue(wentBack.get(), "Expected MapModePanel to invoke its goBack callback on Save");
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
