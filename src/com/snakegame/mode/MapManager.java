package com.snakegame.mode;

import com.snakegame.config.GameSettings;
import com.snakegame.model.GameConfig;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages maps entirely in-memory during runtime, loading resource maps once,
 * adding developer maps on demand, and saving new developer maps both to disk
 * and to the in-memory cache.
 */
public class MapManager {
    private static final int RESOURCE_MAP_COUNT = 10;
    private static final Map<Integer, MapConfig> maps = new HashMap<>();

    static {
        // Load only the packaged resource maps at startup
        for (int i = 1; i <= RESOURCE_MAP_COUNT; i++) {
            String resPath = "/maps/map" + i + ".txt";
            try (InputStream is = MapManager.class.getResourceAsStream(resPath)) {
                if (is == null) continue;
                List<Point> pts = readPoints(new BufferedReader(new InputStreamReader(is)));
                maps.put(i, new MapConfig(i, pts));
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * When developer mode is enabled, load any existing developer maps from disk
     * into the in-memory cache, overriding packaged maps of the same ID.
     */
    public static void loadDeveloperMaps() {
        if (!GameSettings.isDeveloperModeEnabled()) return;
        Path dir = Paths.get("resources", "maps");
        if (!Files.exists(dir)) return;
        Pattern pat = Pattern.compile("map(\\d+)_developer\\.txt");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "map*_developer.txt")) {
            for (Path fp : ds) {
                Matcher m = pat.matcher(fp.getFileName().toString());
                if (!m.matches()) continue;
                int id = Integer.parseInt(m.group(1));
                try (BufferedReader reader = Files.newBufferedReader(fp)) {
                    List<Point> pts = readPoints(reader);
                    maps.put(id, new MapConfig(id, pts));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a developer map both to disk and to the in-memory cache.
     */
    public static void saveMapConfig(int id, List<Point> obstacles) {
        // 1) Write out to developer file
        Path dir = Paths.get("resources", "maps");
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve("map" + id + "_developer.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                for (Point p : obstacles) {
                    int gx = p.x / GameConfig.UNIT_SIZE;
                    int gy = p.y / GameConfig.UNIT_SIZE;
                    writer.write(gx + "," + gy);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 2) Immediately update in-memory cache
        List<Point> pts = new ArrayList<>(obstacles);
        maps.put(id, new MapConfig(id, pts));
    }

    /**
     * Get a sorted list of all map IDs available in memory.
     * Call loadDeveloperMaps() first if dev mode was just enabled.
     */
    public static List<Integer> getMapIds() {
        List<Integer> ids = new ArrayList<>(maps.keySet());
        Collections.sort(ids);
        return ids;
    }

    public static MapConfig getMap(int id) {
        return maps.get(id);
    }

    private static List<Point> readPoints(BufferedReader reader) throws IOException {
        List<Point> pts = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(",");
            int gx = Integer.parseInt(parts[0].trim());
            int gy = Integer.parseInt(parts[1].trim());
            pts.add(new Point(gx * GameConfig.UNIT_SIZE,
                    gy * GameConfig.UNIT_SIZE));
        }
        return pts;
    }
}