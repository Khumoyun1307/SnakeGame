package com.snakegame.ai;

import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Cell}.
 */
class CellTest {

    @Test
    void fromPixel_and_toPixel_roundTrip() {
        Point p = new Point(3 * GameConfig.UNIT_SIZE, 7 * GameConfig.UNIT_SIZE);
        Cell c = Cell.fromPixel(p);
        assertEquals(3, c.x);
        assertEquals(7, c.y);
        assertEquals(p, c.toPixel());
    }

    @Test
    void step_wrapsAroundEdges() {
        int cols = 5;
        int rows = 4;
        Cell c = new Cell(0, 0);

        assertEquals(new Cell(4, 0), c.step(Direction.LEFT, cols, rows));
        assertEquals(new Cell(0, 3), c.step(Direction.UP, cols, rows));
        assertEquals(new Cell(1, 0), c.step(Direction.RIGHT, cols, rows));
        assertEquals(new Cell(0, 1), c.step(Direction.DOWN, cols, rows));
    }

    @Test
    void equals_and_hashCode_workForGridCoordinates() {
        assertEquals(new Cell(1, 2), new Cell(1, 2));
        assertNotEquals(new Cell(1, 2), new Cell(2, 1));
        assertEquals(new Cell(1, 2).hashCode(), new Cell(1, 2).hashCode());
    }
}
