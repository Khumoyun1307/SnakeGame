package com.snakegame.model;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovingObstacleTest {

    @Test
    void constructor_buildsCorrectNumberOfSegments_horizontal() {
        Point start = new Point(0, 0);
        int length = 5;
        boolean vertical = false;
        MovingObstacle mo = new MovingObstacle(
                start,
                length,
                vertical,
                GameConfig.MOVING_OBSTACLE_SPEED,
                new Rectangle(0, 0, 200, 200)
        );

        List<Point> segs = mo.getSegments();
        assertEquals(length, segs.size());
        for (int i = 0; i < length; i++) {
            assertEquals(new Point(i * GameConfig.UNIT_SIZE, 0), segs.get(i));
        }
    }

    @Test
    void constructor_buildsCorrectNumberOfSegments_vertical() {
        Point start = new Point(10, 10);
        int length = 3;
        boolean vertical = true;
        MovingObstacle mo = new MovingObstacle(
                start,
                length,
                vertical,
                GameConfig.MOVING_OBSTACLE_SPEED,
                new Rectangle(0, 0, 200, 200)
        );

        List<Point> segs = mo.getSegments();
        assertEquals(length, segs.size());
        for (int i = 0; i < length; i++) {
            assertEquals(new Point(10, 10 + i * GameConfig.UNIT_SIZE), segs.get(i));
        }
    }

    @Test
    void update_movesAllSegmentsBySpeed() {
        Point start = new Point(50, 50);
        int length = 2;
        MovingObstacle mo = new MovingObstacle(
                start,
                length,
                false,  // horizontal so dx != 0, dy = 0
                GameConfig.MOVING_OBSTACLE_SPEED,
                new Rectangle(0, 0, 500, 500)
        );

        // capture pre‐update positions
        List<Point> before = mo.getSegments().stream()
                .map(Point::new)
                .toList();

        mo.update();

        List<Point> after = mo.getSegments();
        // Each segment must have moved exactly SPEED in x (±) and unchanged in y
        for (int i = 0; i < length; i++) {
            Point b = before.get(i);
            Point a = after.get(i);

            int dx = Math.abs(a.x - b.x);
            int dy = Math.abs(a.y - b.y);

            assertEquals(GameConfig.MOVING_OBSTACLE_SPEED, dx, "Should move by speed in X");
            assertEquals(0, dy, "Y should not change for horizontal obstacle");
        }
    }
}
