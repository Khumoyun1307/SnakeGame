package com.snakegame.model;

import java.awt.*;

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

    public int getScoreValue() {
        return scoreValue;
    }

    public Color getColor() {
        return color;
    }
}
