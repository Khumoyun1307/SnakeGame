package com.snakegame.ui.flow;

import com.snakegame.config.GameSettings;
import com.snakegame.config.GameSettingsManager;
import com.snakegame.mode.GameMode;
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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class ManualFlowPersistenceTest extends SnakeTestBase {

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
    void mapModePanelSave_persistsModeAndMapId() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path settingsPath = tmp.resolve("settings.properties");
            GameSettingsManager.setFilePath(settingsPath.toString());

            ProgressManager.setFilePath(tmp.resolve("progress.txt").toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();
            ProgressManager.unlockMap(3);

            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            GameSettings.setCurrentMode(GameMode.STANDARD);
            GameSettings.setSelectedMapId(1);

            selectMapInPanelAndSave("Map 3");

            Properties p = loadProps(settingsPath);
            assertEquals("MAP_SELECT", p.getProperty("currentMode"));
            assertEquals("3", p.getProperty("selectedMapId"));
        }
    }

    @Test
    void startRace_persistsRaceAndResetsToMap1() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path settingsPath = tmp.resolve("settings.properties");
            GameSettingsManager.setFilePath(settingsPath.toString());

            ProgressManager.setFilePath(tmp.resolve("progress.txt").toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();
            ProgressManager.unlockMap(3);

            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            selectMapInPanelAndSave("Map 3");
            assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());

            apply(MenuFlowDecider.decideRace(state()));

            Properties p = loadProps(settingsPath);
            assertEquals("RACE", p.getProperty("currentMode"));
            assertEquals("1", p.getProperty("selectedMapId"));
        }
    }

    @Test
    void load_roundTripsPanelSelection() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            Path settingsPath = tmp.resolve("settings.properties");
            GameSettingsManager.setFilePath(settingsPath.toString());

            ProgressManager.setFilePath(tmp.resolve("progress.txt").toString());
            ProgressManager.clearUnlockedMaps();
            ProgressManager.load();
            ProgressManager.unlockMap(3);

            GameSaveManager.setFilePath(tmp.resolve("savegame.txt").toString());
            GameSaveManager.clearSave();

            GameSettings.setDeveloperModeEnabled(false);
            selectMapInPanelAndSave("Map 3");

            GameSettings.withAutosaveSuppressed(() -> {
                GameSettings.setCurrentMode(GameMode.STANDARD);
                GameSettings.setSelectedMapId(1);
            });

            GameSettingsManager.load();

            assertEquals(GameMode.MAP_SELECT, GameSettings.getCurrentMode());
            assertEquals(3, GameSettings.getSelectedMapId());
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

    private static Properties loadProps(Path path) throws Exception {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        return p;
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

