package com.snakegame.mode;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapConfigTest {
    @Test
    void gettersReturnConstructorValues() {
        var pts = List.of(new Point(0, 0), new Point(25, 50));
        MapConfig cfg = new MapConfig(7, pts);
        assertEquals(7, cfg.getId());
        assertSame(pts, cfg.getObstacles());
    }
}
