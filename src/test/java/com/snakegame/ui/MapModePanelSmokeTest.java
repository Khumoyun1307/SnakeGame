package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SnakeTestBase;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class MapModePanelSmokeTest extends SnakeTestBase {

    @Test
    void selectingBasicMapAndSaving_setsStandardAndResetsMapId() throws Exception {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameSettings.setDeveloperModeEnabled(false);
            GameSettings.setCurrentMode(GameMode.MAP_SELECT);
            GameSettings.setSelectedMapId(5);

            AtomicBoolean wentBack = new AtomicBoolean(false);
            MapModePanel[] panel = new MapModePanel[1];
            SwingUtilities.invokeAndWait(() -> panel[0] = new MapModePanel(() -> wentBack.set(true)));

            AbstractButton basic = findButton(panel[0], b -> "Basic Map".equals(b.getText()));
            AbstractButton save = findButton(panel[0], b -> {
                String t = b.getText();
                return t != null && t.contains("Save");
            });

            SwingUtilities.invokeAndWait(basic::doClick);
            SwingUtilities.invokeAndWait(save::doClick);

            assertTrue(wentBack.get());
            assertEquals(GameMode.STANDARD, GameSettings.getCurrentMode());
            assertEquals(1, GameSettings.getSelectedMapId());
        }
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

