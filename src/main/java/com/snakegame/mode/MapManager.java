package com.snakegame.mode;

import com.snakegame.config.GameSettings;
import com.snakegame.model.GameConfig;
import com.snakegame.util.AppPaths;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages maps entirely in-memory during runtime, loading resource maps once,
 * adding developer maps on demand, and saving new developer maps both to disk
 * and to the in-memory cache.
 */
public class MapManager {
    private static final int RESOURCE_MAP_COUNT = 10;
    private static final Map<Integer, MapConfig> maps = new HashMap<>();
    private static final Logger log = Logger.getLogger(MapManager.class.getName());

    private static final String DEV_MAP_GLOB = "map*_developer.txt";
    private static final Pattern DEV_MAP_PATTERN = Pattern.compile("map(\\d+)_developer\\.txt");

    /** Developer maps are stored in a per-user writable directory (not inside resources/ or the jar). */
    private static final Path DEV_MAPS_DIR = AppPaths.DATA_DIR.resolve("dev-maps");

    /** Legacy developer map location from older builds / local dev runs. */
    private static final Path LEGACY_DEV_MAPS_DIR = Paths.get("resources", "maps");

    static {
        // Load only the packaged resource maps at startup
        for (int i = 1; i <= RESOURCE_MAP_COUNT; i++) {
            String resPath = "/maps/map" + i + ".txt";
            try (InputStream is = MapManager.class.getResourceAsStream(resPath)) {
                if (is == null) continue;
                List<Point> pts = readPoints(new BufferedReader(new InputStreamReader(is)));
                maps.put(i, new MapConfig(i, pts));
            } catch (IOException | NumberFormatException e) {
                log.log(Level.WARNING, "Failed to load resource map: " + resPath, e);
            }
        }
    }

    /**
     * When developer mode is enabled, load any existing developer maps from disk
     * into the in-memory cache, overriding packaged maps of the same ID.
     */
    public static void loadDeveloperMaps() {
        if (!GameSettings.isDeveloperModeEnabled()) return;

        migrateLegacyDeveloperMapsIfPresent();
        loadDeveloperMapsFromDirectory(DEV_MAPS_DIR);
    }

    /**
     * Saves a developer map both to disk and to the in-memory cache.
     */
    public static void saveMapConfig(int id, List<Point> obstacles) {
        // 1) Write out to developer file
        Path dir = DEV_MAPS_DIR;
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
            log.log(Level.WARNING, "Failed to save developer map: " + id, e);
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

    /**
     * Returns the map configuration for the given id, if loaded.
     *
     * @param id map id
     * @return map configuration, or {@code null} if not found
     */
    public static MapConfig getMap(int id) {
        return maps.get(id);
    }

    /** True if this id exists in the packaged (non-developer) map set. */
    public static boolean isPackagedMapId(int id) {
        return id >= 1 && id <= RESOURCE_MAP_COUNT;
    }

    private static void loadDeveloperMapsFromDirectory(Path dir) {
        if (!Files.isDirectory(dir)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, DEV_MAP_GLOB)) {
            for (Path fp : ds) {
                Matcher m = DEV_MAP_PATTERN.matcher(fp.getFileName().toString());
                if (!m.matches()) continue;
                int id = Integer.parseInt(m.group(1));
                try (BufferedReader reader = Files.newBufferedReader(fp)) {
                    List<Point> pts = readPoints(reader);
                    maps.put(id, new MapConfig(id, pts));
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load developer maps from: " + dir, e);
        }
    }

    /**
     * Best-effort migration from the legacy developer-map folder to the per-user AppPaths directory.
     *
     * <p>Copies files once and does not delete the legacy originals automatically.</p>
     */
    private static void migrateLegacyDeveloperMapsIfPresent() {
        if (!Files.isDirectory(LEGACY_DEV_MAPS_DIR)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(LEGACY_DEV_MAPS_DIR, DEV_MAP_GLOB)) {
            Files.createDirectories(DEV_MAPS_DIR);
            for (Path legacyFile : ds) {
                Path target = DEV_MAPS_DIR.resolve(legacyFile.getFileName().toString());
                if (Files.exists(target)) continue;
                try {
                    Files.copy(legacyFile, target);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to migrate developer map: " + legacyFile, e);
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to scan legacy developer maps from: " + LEGACY_DEV_MAPS_DIR, e);
        }
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
