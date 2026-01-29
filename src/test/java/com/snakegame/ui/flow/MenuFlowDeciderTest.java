package com.snakegame.ui.flow;

import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SnakeTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuFlowDeciderTest extends SnakeTestBase {

    @Test
    void play_forcesStandardUnlessMapSelectChosen() {
        var d1 = MenuFlowDecider.decidePlay(new MenuFlowDecider.MenuState(GameMode.AI, 1, false));
        assertEquals(GameMode.STANDARD, d1.modeToSet());

        var d2 = MenuFlowDecider.decidePlay(new MenuFlowDecider.MenuState(GameMode.MAP_SELECT, 5, false));
        assertEquals(GameMode.MAP_SELECT, d2.modeToSet());
    }

    @Test
    void play_sanitizesDeveloperOnlyMapSelectionWhenDevModeOff() {
        var d = MenuFlowDecider.decidePlay(new MenuFlowDecider.MenuState(GameMode.MAP_SELECT, 88, false));
        assertEquals(GameMode.STANDARD, d.modeToSet());
        assertEquals(Integer.valueOf(1), d.selectedMapIdToSet());
    }

    @Test
    void play_doesNotSanitizeMapWhenDevModeOn() {
        var d = MenuFlowDecider.decidePlay(new MenuFlowDecider.MenuState(GameMode.MAP_SELECT, 88, true));
        assertEquals(GameMode.MAP_SELECT, d.modeToSet());
        assertNull(d.selectedMapIdToSet());
    }

    @Test
    void race_alwaysStartsAtMap1() {
        var d = MenuFlowDecider.decideRace(new MenuFlowDecider.MenuState(GameMode.STANDARD, 7, false));
        assertEquals(GameMode.RACE, d.modeToSet());
        assertEquals(Integer.valueOf(1), d.selectedMapIdToSet());
    }

    @Test
    void ai_setsModeOnly() {
        var d = MenuFlowDecider.decideAi(new MenuFlowDecider.MenuState(GameMode.STANDARD, 5, false));
        assertEquals(GameMode.AI, d.modeToSet());
        assertNull(d.selectedMapIdToSet());
    }
}

