package com.snakegame.model;

import java.awt.*;
import java.util.Random;
import java.util.Set;

public class Apple {
    private final Random random = new Random();
    private Point position;
    private AppleType type;

    private long spawnTime;
    private long visibleDurationMs;

    public Apple(Set<Point> forbidden) {
        spawnNew(AppleType.NORMAL, forbidden);
    }

    public void spawnNew(AppleType type, Set<Point> forbiddenPositions) {
        int maxX = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int maxY = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;

        Random rand = new Random();
        Point newPos;
        do {
            int x = rand.nextInt(maxX) * GameConfig.UNIT_SIZE;
            int y = rand.nextInt(maxY) * GameConfig.UNIT_SIZE;
            newPos = new Point(x, y);
        } while (forbiddenPositions.contains(newPos));

        this.position = newPos;
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
        this.visibleDurationMs = switch (type) {
            case BIG -> GameConfig.BIG_VISIBLE_DURATION_MS;
            case GOLDEN -> GameConfig.GOLDEN_VISIBLE_DURATION_MS;
            case SLOWDOWN -> GameConfig.SLOWDOWN_VISIBLE_DURATION_MS;
            default -> 0;
        };
    }


    public boolean isExpired() {
        if (visibleDurationMs <= 0) return false;
        return System.currentTimeMillis() - spawnTime > visibleDurationMs;
    }

    public void spawnRandomlyWeighted(int applesEaten, int score, Set<Point> forbidden) {
        if (score > 0 && score % GameConfig.REVERSE_APPLE_EVERY_SCORE == 0) {
            spawnNew(AppleType.REVERSE, forbidden);
        } else if (applesEaten > 0 && applesEaten % GameConfig.SLOWDOWN_APPLE_EVERY == 0) {
            spawnNew(AppleType.SLOWDOWN,forbidden);
        } else if (applesEaten > 0 && applesEaten % GameConfig.GOLDEN_APPLE_EVERY == 0) {
            spawnNew(AppleType.GOLDEN,forbidden);
        } else if (applesEaten > 0 && applesEaten % GameConfig.BIG_APPLE_EVERY == 0) {
            spawnNew(AppleType.BIG,forbidden);
        } else {
            spawnNew(AppleType.NORMAL,forbidden);
        }
    }


    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public AppleType getType() {
        return type;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public long getVisibleDurationMs() {
        return visibleDurationMs;
    }

}
