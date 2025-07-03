package com.snakegame.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A line‐segment obstacle that moves and bounces within the play area.
 */
public class MovingObstacle {
    private final List<Point> segments;
    private int dx, dy;
    private final Rectangle bounds;

    /**
     * @param start     head coordinate (top‐left) in pixels
     * @param length    number of units long (1–7)
     * @param vertical  true=vertical line, false=horizontal
     * @param speed     pixels per update tick
     * @param area      play‐area rectangle in pixels
     */
    public MovingObstacle(Point start, int length, boolean vertical, int speed, Rectangle area) {
        this.segments = new ArrayList<>();
        this.bounds = area;

        // Build segment list
        for (int i = 0; i < length; i++) {
            int x = start.x + (vertical ? 0 : i * GameConfig.UNIT_SIZE);
            int y = start.y + (vertical ? i * GameConfig.UNIT_SIZE : 0);
            segments.add(new Point(x, y));
        }

        // Random initial direction
        if (vertical) {
            dx = 0;
            dy = (Math.random() < 0.5 ? 1 : -1) * speed;
        } else {
            dx = (Math.random() < 0.5 ? 1 : -1) * speed;
            dy = 0;
        }
    }

    /**
     * Move all segments by (dx,dy). Bounce if head or tail hits bounds.
     */
    public void update() {
        // Move
        for (Point p : segments) {
            p.translate(dx, dy);
        }
        // Bounce on X
        boolean bounceX = segments.stream().anyMatch(p ->
                p.x < bounds.x || p.x + GameConfig.UNIT_SIZE > bounds.x + bounds.width);
        if (bounceX) dx = -dx;
        // Bounce on Y
        boolean bounceY = segments.stream().anyMatch(p ->
                p.y < bounds.y || p.y + GameConfig.UNIT_SIZE > bounds.y + bounds.height);
        if (bounceY) dy = -dy;
    }

    /**
     * @return list of head‐to‐tail pixel positions
     */
    public List<Point> getSegments() {
        return segments;
    }
}
