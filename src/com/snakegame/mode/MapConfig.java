package com.snakegame.mode;

import java.awt.Point;
import java.util.List;

/**
 * Holds the configuration for a single map: its ID and fixed obstacle layout.
 */
public class MapConfig {
    private final int id;
    private final List<Point> obstacles;

    public MapConfig(int id, List<Point> obstacles) {
        this.id = id;
        this.obstacles = obstacles;
    }

    public int getId() {
        return id;
    }

    public List<Point> getObstacles() {
        return obstacles;
    }
}