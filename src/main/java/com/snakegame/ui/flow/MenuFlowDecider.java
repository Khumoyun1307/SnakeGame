package com.snakegame.ui.flow;

import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapManager;

/**
 * Pure decision logic for main-menu actions that start gameplay.
 *
 * <p>This keeps UI wiring in Swing classes while making mode/map transitions unit-testable.</p>
 */
public final class MenuFlowDecider {
    private MenuFlowDecider() {}

    public record MenuState(GameMode currentMode, int selectedMapId, boolean developerModeEnabled) {
        public MenuState {
            if (currentMode == null) currentMode = GameMode.STANDARD;
        }
    }

    public record StartDecision(GameMode modeToSet, Integer selectedMapIdToSet, boolean clearSavedGameIfAllowed) {}

    public static StartDecision decidePlay(MenuState state) {
        GameMode modeToSet = (state.currentMode == GameMode.MAP_SELECT) ? GameMode.MAP_SELECT : GameMode.STANDARD;
        Integer selectedMapIdToSet = null;

        if (!state.developerModeEnabled) {
            int mapId = state.selectedMapId;
            if (mapId > 0 && !MapManager.isPackagedMapId(mapId)) {
                selectedMapIdToSet = 1;
                modeToSet = GameMode.STANDARD;
            }
        }

        return new StartDecision(modeToSet, selectedMapIdToSet, true);
    }

    public static StartDecision decideRace(MenuState state) {
        return new StartDecision(GameMode.RACE, 1, true);
    }

    public static StartDecision decideAi(MenuState state) {
        return new StartDecision(GameMode.AI, null, true);
    }
}

