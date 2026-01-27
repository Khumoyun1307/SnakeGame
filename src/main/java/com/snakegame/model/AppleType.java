package com.snakegame.model;

import java.awt.*;

/**
 * Enumerates the different apple types (power-ups) available in the game.
 *
 * <p>Each type defines a score value and a rendering color. Additional effects (double score,
 * slowdown, reverse controls, etc.) are applied by the simulation layer.</p>
 */
public enum AppleType {
    NORMAL(1, Color.RED),
    BIG(4, Color.ORANGE),
    GOLDEN(2, Color.YELLOW),
    SLOWDOWN(1, Color.CYAN),
    REVERSE(1, Color.MAGENTA);

    private final int scoreValue;
    private final Color color;

    AppleType(int scoreValue, Color color) {
        this.scoreValue = scoreValue;
        this.color = color;
    }

    /**
     * Returns the number of points awarded when this apple is eaten.
     *
     * @return score value for this apple type
     */
    public int getScoreValue() {
        return scoreValue;
    }

    /**
     * Returns the base color used to render this apple type.
     *
     * @return apple color
     */
    public Color getColor() {
        return color;
    }
}
