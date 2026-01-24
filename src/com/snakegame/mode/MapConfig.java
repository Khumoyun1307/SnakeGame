package com.snakegame.mode;

import java.awt.Point;
import java.util.List;

/**
 * Holds the configuration for a single map: its ID and fixed obstacle layout.
 */
public class MapConfig {
    private final int id;
    private final List<Point> obstacles;

    /**
     * Creates a map configuration with an id and a fixed obstacle layout.
     *
     * @param id map identifier
     * @param obstacles obstacle positions (pixel coordinates)
     */
    public MapConfig(int id, List<Point> obstacles) {
        this.id = id;
        this.obstacles = obstacles;
    }

    /**
     * Returns this map's id.
     *
     * @return map id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the list of obstacles for this map.
     *
     * @return obstacle positions (pixel coordinates)
     */
    public List<Point> getObstacles() {
        return obstacles;
    }
}
