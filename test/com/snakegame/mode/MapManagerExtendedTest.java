package com.snakegame.mode;

import com.snakegame.config.GameSettings;
import com.snakegame.model.GameConfig;
import org.junit.jupiter.api.*;

import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapManagerExtendedTest {
    private static final int TEST_ID = 99;
    private static final Path DEV_DIR = Paths.get("resources", "maps");
    private static final Path DEV_FILE = DEV_DIR.resolve("map" + TEST_ID + "_developer.txt");
    private boolean origDevMode;
    private byte[] backupBytes;

    @BeforeEach
    void setUp() throws Exception {
        // Backup and delete any existing developer file
        if (Files.exists(DEV_FILE)) {
            backupBytes = Files.readAllBytes(DEV_FILE);
        }
        Files.deleteIfExists(DEV_FILE);
        // Enable developer mode
        origDevMode = GameSettings.isDeveloperModeEnabled();
        GameSettings.setDeveloperModeEnabled(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore developer mode
        GameSettings.setDeveloperModeEnabled(origDevMode);
        // Remove test entry from cache
        Field mapsField = MapManager.class.getDeclaredField("maps");
        mapsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var maps = (java.util.Map<Integer, MapConfig>) mapsField.get(null);
        maps.remove(TEST_ID);
        // Restore or delete file
        if (backupBytes != null) {
            Files.createDirectories(DEV_DIR);
            Files.write(DEV_FILE, backupBytes);
        } else {
            Files.deleteIfExists(DEV_FILE);
        }
    }

    @Test
    void saveMapConfig_writesFileAndUpdatesCache() throws IOException {
        List<Point> pts = List.of(
                new Point(0, 0),
                new Point(GameConfig.UNIT_SIZE, GameConfig.UNIT_SIZE)
        );

        assertNull(MapManager.getMap(TEST_ID));
        MapManager.saveMapConfig(TEST_ID, pts);

        assertTrue(Files.exists(DEV_FILE), "Developer map file should be created");
        List<String> lines = Files.readAllLines(DEV_FILE);
        assertEquals("0,0", lines.get(0).trim());
        assertEquals("1,1", lines.get(1).trim());

        MapConfig cfg = MapManager.getMap(TEST_ID);
        assertNotNull(cfg);
        assertEquals(TEST_ID, cfg.getId());
        assertEquals(pts, cfg.getObstacles());
    }
}
