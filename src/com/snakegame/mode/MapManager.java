package com.snakegame.mode;

import com.snakegame.model.GameConfig;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and provides access to all MapConfig instances defined in text files under /maps/.
 */
public class MapManager {
    private static final int TOTAL_MAPS = 10;
    private static final Map<Integer, MapConfig> maps = new HashMap<>();

    static {
        for (int i = 1; i <= TOTAL_MAPS; i++) {
            MapConfig config = loadMapConfig(i);
            if (config != null) {
                maps.put(i, config);
            }
        }
    }

    private static MapConfig loadMapConfig(int id) {
        String path = "/maps/map" + id + ".txt";
        try (InputStream is = MapManager.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Map file not found: " + path);
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            List<Point> obstacles = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                int gridX = Integer.parseInt(parts[0].trim());
                int gridY = Integer.parseInt(parts[1].trim());
                // Convert grid cell to pixel position
                obstacles.add(new Point(
                        gridX * GameConfig.UNIT_SIZE,
                        gridY * GameConfig.UNIT_SIZE
                ));
            }
            return new MapConfig(id, obstacles);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve the MapConfig by its ID (1 through TOTAL_MAPS).
     */
    public static MapConfig getMap(int id) {
        return maps.get(id);
    }

    public static int getTotalMaps() {
        return maps.size();
    }
}