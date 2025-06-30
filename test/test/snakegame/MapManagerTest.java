package test.snakegame;

import com.snakegame.mode.MapConfig;
import com.snakegame.mode.MapManager;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapManagerTest {
    @Test
    void testGetMap1ObstaclesNotEmpty() {
        MapConfig map1 = MapManager.getMap(1);
        assertNotNull(map1);
        List<Point> obs = map1.getObstacles();
        assertFalse(obs.isEmpty(), "Map1 should have at least one obstacle");
        assertTrue(obs.contains(new Point(0, 0)), "Expected border obstacle at (0,0)");
    }

    @Test
    void testInvalidMapReturnsNull() {
        assertNull(MapManager.getMap(999));
    }
}