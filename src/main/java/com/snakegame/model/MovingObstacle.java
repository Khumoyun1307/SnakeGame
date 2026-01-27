package com.snakegame.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A line-segment obstacle that moves and bounces within the play area.
 */
public class MovingObstacle {
    private final List<Point> segments;
    private int dx, dy;
    private final Rectangle bounds;

    /**
     * Creates a moving obstacle as a contiguous line of segments.
     *
     * @param start starting position (pixel coordinates)
     * @param length number of segments
     * @param vertical whether the obstacle is vertical (otherwise horizontal)
     * @param speed movement speed in pixels per tick
     * @param area movement bounds
     * @param rng random source used to choose initial direction deterministically
     */
    public MovingObstacle(Point start, int length, boolean vertical, int speed, Rectangle area, Random rng) {
        this.segments = new ArrayList<>();
        this.bounds = area;

        for (int i = 0; i < length; i++) {
            int x = start.x + (vertical ? 0 : i * GameConfig.UNIT_SIZE);
            int y = start.y + (vertical ? i * GameConfig.UNIT_SIZE : 0);
            segments.add(new Point(x, y));
        }

        // Deterministic initial direction
        if (vertical) {
            dx = 0;
            dy = (rng.nextBoolean() ? 1 : -1) * speed;
        } else {
            dx = (rng.nextBoolean() ? 1 : -1) * speed;
            dy = 0;
        }
    }

    /**
     * Advances the obstacle by one tick, bouncing off bounds edges by inverting velocity.
     */
    public void update() {
        for (Point p : segments) {
            p.translate(dx, dy);
        }

        boolean bounceX = segments.stream().anyMatch(p ->
                p.x < bounds.x || p.x + GameConfig.UNIT_SIZE > bounds.x + bounds.width);
        if (bounceX) dx = -dx;

        boolean bounceY = segments.stream().anyMatch(p ->
                p.y < bounds.y || p.y + GameConfig.UNIT_SIZE > bounds.y + bounds.height);
        if (bounceY) dy = -dy;
    }

    /**
     * Returns the list of obstacle segments.
     *
     * @return segment positions (pixel coordinates)
     */
    public List<Point> getSegments() { return segments; }
    /**
     * Returns the x velocity component in pixels per tick.
     *
     * @return dx velocity
     */
    public int getDx() { return dx; }
    /**
     * Returns the y velocity component in pixels per tick.
     *
     * @return dy velocity
     */
    public int getDy() { return dy; }

    /** Backward compatible restore (no RNG needed because dx/dy are stored) */
    public static MovingObstacle fromSnapshot(MovingObstacleSnapshot snap, Rectangle area) {
        return fromSnapshot(snap, area, new Random(0));
    }

    /** Overload so your GameState can pass rng without breaking signature changes */
    public static MovingObstacle fromSnapshot(MovingObstacleSnapshot snap, Rectangle area, Random rng) {
        MovingObstacle mo = new MovingObstacle(
                new Point(0, 0),
                1,
                true,
                GameConfig.MOVING_OBSTACLE_SPEED,
                area,
                rng
        );

        mo.segments.clear();
        if (snap.segments != null) {
            for (Point p : snap.segments) mo.segments.add(new Point(p));
        }
        mo.dx = snap.dx;
        mo.dy = snap.dy;

        return mo;
    }
}
