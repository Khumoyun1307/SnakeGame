package com.snakegame.ui;

import com.snakegame.ai.AiMode;
import com.snakegame.testutil.SnakeTestBase;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class AiModePanelSmokeTest extends SnakeTestBase {

    @Test
    void clickingModeButtons_invokesOnStartWithExpectedMode() throws Exception {
        AtomicReference<AiMode> selected = new AtomicReference<>();
        AiModePanel[] panel = new AiModePanel[1];

        SwingUtilities.invokeAndWait(() -> panel[0] = new AiModePanel(() -> { }, selected::set));

        AbstractButton chase = findButton(panel[0], b -> textContains(b, "Chase"));
        AbstractButton safe = findButton(panel[0], b -> textContains(b, "Safe"));
        AbstractButton survival = findButton(panel[0], b -> textContains(b, "Survival"));

        SwingUtilities.invokeAndWait(chase::doClick);
        assertEquals(AiMode.CHASE, selected.get());

        SwingUtilities.invokeAndWait(safe::doClick);
        assertEquals(AiMode.SAFE, selected.get());

        SwingUtilities.invokeAndWait(survival::doClick);
        assertEquals(AiMode.SURVIVAL, selected.get());
    }

    @Test
    void clickingBackButton_invokesOnBack() throws Exception {
        AtomicBoolean back = new AtomicBoolean(false);
        AiModePanel[] panel = new AiModePanel[1];

        SwingUtilities.invokeAndWait(() -> panel[0] = new AiModePanel(() -> back.set(true), mode -> { }));

        AbstractButton backButton = findButton(panel[0], b -> textContains(b, "Back"));
        SwingUtilities.invokeAndWait(backButton::doClick);
        assertTrue(back.get());
    }

    private static boolean textContains(AbstractButton b, String needle) {
        String t = b.getText();
        return t != null && t.contains(needle);
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

