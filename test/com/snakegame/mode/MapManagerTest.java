package com.snakegame.mode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MapManager}.
 */
class MapManagerTest {

    @Test
    void packagedMaps_areLoadedAndExposedViaIds() {
        var ids = MapManager.getMapIds();
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(10));
        assertTrue(ids.size() >= 10);
        assertNotNull(MapManager.getMap(1));
        assertNotNull(MapManager.getMap(10));
    }

    @Test
    void isPackagedMapId_matchesResourceRange() {
        assertTrue(MapManager.isPackagedMapId(1));
        assertTrue(MapManager.isPackagedMapId(10));
        assertFalse(MapManager.isPackagedMapId(0));
        assertFalse(MapManager.isPackagedMapId(11));
    }
}
