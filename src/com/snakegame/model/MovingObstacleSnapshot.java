package com.snakegame.model;

import java.awt.Point;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MovingObstacleSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public List<Point> segments = new ArrayList<>();
    public int dx;
    public int dy;

    public MovingObstacleSnapshot() {}

    public MovingObstacleSnapshot(List<Point> segments, int dx, int dy) {
        if (segments != null) {
            for (Point p : segments) this.segments.add(new Point(p));
        }
        this.dx = dx;
        this.dy = dy;
    }

    public static MovingObstacleSnapshot from(MovingObstacle mo) {
        MovingObstacleSnapshot s = new MovingObstacleSnapshot();
        for (Point p : mo.getSegments()) s.segments.add(new Point(p));
        s.dx = mo.getDx();
        s.dy = mo.getDy();
        return s;
    }
}
