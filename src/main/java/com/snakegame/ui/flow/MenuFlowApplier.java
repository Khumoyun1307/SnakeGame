package com.snakegame.ui.flow;

import com.snakegame.config.GameSettings;
import com.snakegame.util.ProgressManager;

/**
 * Applies a {@link MenuFlowDecider.StartDecision} to global settings/persistence.
 *
 * <p>This keeps the decision logic pure while centralizing the side-effect application.</p>
 */
public final class MenuFlowApplier {
    private MenuFlowApplier() {}

    /**
     * Applies a start decision to {@link GameSettings} and clears a saved game when appropriate.
     *
     * <p>Behavior intentionally mirrors the prior {@code GameFrame} inlined logic.</p>
     *
     * @param decision decision to apply (ignored if null)
     */
    public static void applyStartDecision(MenuFlowDecider.StartDecision decision) {
        if (decision == null) return;

        if (decision.modeToSet() != GameSettings.getCurrentMode()) {
            GameSettings.setCurrentMode(decision.modeToSet());
        }
        if (decision.selectedMapIdToSet() != null
                && decision.selectedMapIdToSet() != GameSettings.getSelectedMapId()) {
            GameSettings.setSelectedMapId(decision.selectedMapIdToSet());
        }
        if (decision.clearSavedGameIfAllowed()) {
            ProgressManager.clearSavedGameIfAllowed(GameSettings.isDeveloperModeEnabled());
        }
    }
}

