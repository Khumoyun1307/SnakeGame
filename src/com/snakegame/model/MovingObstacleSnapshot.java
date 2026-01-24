package com.snakegame.model;

import java.awt.Point;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of a {@link MovingObstacle} used for save/continue persistence.
 *
 * <p>Stores the obstacle's segments and velocity components so the moving obstacle can be restored
 * without re-rolling randomness.</p>
 */
public class MovingObstacleSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public List<Point> segments = new ArrayList<>();
    public int dx;
    public int dy;

    /**
     * Creates an empty snapshot.
     */
    public MovingObstacleSnapshot() {}

    /**
     * Creates a snapshot from the provided segment list and velocity.
     *
     * @param segments obstacle segments (pixel coordinates)
     * @param dx velocity in pixels per tick on the x-axis
     * @param dy velocity in pixels per tick on the y-axis
     */
    public MovingObstacleSnapshot(List<Point> segments, int dx, int dy) {
        if (segments != null) {
            for (Point p : segments) this.segments.add(new Point(p));
        }
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Creates a snapshot from a live moving obstacle instance.
     *
     * @param mo moving obstacle to snapshot
     * @return snapshot capturing segments and velocity
     */
    public static MovingObstacleSnapshot from(MovingObstacle mo) {
        MovingObstacleSnapshot s = new MovingObstacleSnapshot();
        for (Point p : mo.getSegments()) s.segments.add(new Point(p));
        s.dx = mo.getDx();
        s.dy = mo.getDy();
        return s;
    }
}
