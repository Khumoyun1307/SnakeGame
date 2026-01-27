package com.snakegame.view;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.model.GameState;
import com.snakegame.testutil.SettingsGuard;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Smoke test for {@link com.snakegame.view.GameRenderer}.
 */
class GameRendererSmokeTest {

    private static SettingsSnapshot settings(boolean showGrid, boolean movingObstacles) {
        return new SettingsSnapshot(
                20,
                false,
                GameMode.STANDARD,
                1,
                20,
                false,
                false,
                showGrid,
                "Render",
                UUID.randomUUID(),
                GameSettings.Theme.RETRO,
                movingObstacles,
                0,
                false,
                false
        );
    }

    @Test
    void renderScaleToFit_doesNotThrow_forLiveAndSnapshotSettings() {
        try (SettingsGuard ignored = new SettingsGuard()) {
            GameState state = new GameState(1L, false, settings(true, false));
            BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                assertDoesNotThrow(() -> GameRenderer.renderScaleToFit(g, state, 800, 600));
                assertDoesNotThrow(() -> GameRenderer.renderScaleToFit(g, state, 800, 600, settings(false, false)));
            } finally {
                g.dispose();
            }
        }
    }
}
