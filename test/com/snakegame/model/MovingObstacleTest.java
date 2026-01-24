package com.snakegame.model;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MovingObstacle}.
 */
class MovingObstacleTest {

    @Test
    void constructor_buildsSegmentsHorizontallyOrVertically() {
        Rectangle area = new Rectangle(0, 0, 100, 100);
        MovingObstacle horizontal = new MovingObstacle(new Point(0, 0), 3, false, GameConfig.UNIT_SIZE, area, new Random(1));
        assertEquals(List.of(new Point(0, 0), new Point(25, 0), new Point(50, 0)), horizontal.getSegments());
        assertEquals(0, horizontal.getDy());
        assertEquals(GameConfig.UNIT_SIZE, Math.abs(horizontal.getDx()));

        MovingObstacle vertical = new MovingObstacle(new Point(0, 0), 3, true, GameConfig.UNIT_SIZE, area, new Random(1));
        assertEquals(List.of(new Point(0, 0), new Point(0, 25), new Point(0, 50)), vertical.getSegments());
        assertEquals(0, vertical.getDx());
        assertEquals(GameConfig.UNIT_SIZE, Math.abs(vertical.getDy()));
    }

    @Test
    void update_translatesAllSegments() {
        Rectangle area = new Rectangle(0, 0, 200, 200);
        MovingObstacleSnapshot snap = new MovingObstacleSnapshot(
                List.of(new Point(25, 25), new Point(50, 25)),
                GameConfig.UNIT_SIZE,
                0
        );
        MovingObstacle mo = MovingObstacle.fromSnapshot(snap, area, new Random(0));

        mo.update();
        assertEquals(List.of(new Point(50, 25), new Point(75, 25)), mo.getSegments());
    }

    @Test
    void update_bouncesWhenCrossingBounds() {
        Rectangle area = new Rectangle(0, 0, 50, 50); // 2x2 cells
        MovingObstacleSnapshot snap = new MovingObstacleSnapshot(
                List.of(new Point(25, 0)),
                GameConfig.UNIT_SIZE,
                0
        );
        MovingObstacle mo = MovingObstacle.fromSnapshot(snap, area, new Random(0));

        mo.update(); // moves to x=50, then bounce triggers (x+UNIT_SIZE > width)
        assertEquals(-GameConfig.UNIT_SIZE, mo.getDx());
    }
}
