package com.snakegame.model;

import java.awt.*;
import java.util.Set;

public class AppleSpawner {
    public static Apple spawnNextApple(int applesEaten, int score, Set<Point> forbidden) {
        AppleType type;

        if (score > 0 && score % GameConfig.REVERSE_APPLE_EVERY_SCORE == 0) {
            type = AppleType.REVERSE;
        } else if (applesEaten > 0 && applesEaten % GameConfig.SLOWDOWN_APPLE_EVERY == 0) {
            type = AppleType.SLOWDOWN;
        } else if (applesEaten > 0 && applesEaten % GameConfig.GOLDEN_APPLE_EVERY == 0) {
            type = AppleType.GOLDEN;
        } else if (applesEaten > 0 && applesEaten % GameConfig.BIG_APPLE_EVERY == 0) {
            type = AppleType.BIG;
        } else {
            type = AppleType.NORMAL;
        }

        Apple apple = new Apple(forbidden);
        apple.spawnNew(type, forbidden);
        return apple;
    }
}
