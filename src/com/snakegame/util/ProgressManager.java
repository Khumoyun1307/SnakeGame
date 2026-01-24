package com.snakegame.util;

import com.snakegame.model.GameSnapshot;
import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists and provides the set of unlocked maps for MAP_SELECT and RACE modes.
 */
public class ProgressManager {
    private static final Logger log = Logger.getLogger(ProgressManager.class.getName());
    private static String filePath = AppPaths.PROGRESS_FILE.toString();
    private static final Set<Integer> unlockedMaps = new HashSet<>();

    static {
        load();
    }

    /**
     * Sets a custom file path for testing or alternate storage.
     */
    public static void setFilePath(String path) {
        filePath = path;
    }

    /**
     * Clears in-memory unlocked maps. For testing only.
     */
    public static void clearUnlockedMaps() {
        unlockedMaps.clear();
    }

    /**
     * Loads unlocked maps from the configured file path.
     * If the file does not exist, defaults to map 1 unlocked.
     */
    public static void load() {
        unlockedMaps.clear();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            unlockedMaps.add(1);
            save();
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.startsWith("unlockedMaps")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        String[] ids = parts[1].split(",");
                        for (String id : ids) {
                            unlockedMaps.add(Integer.parseInt(id.trim()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load progress from: " + filePath, e);
        }
    }

    private static void save() {
        String joined = unlockedMaps.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String line = "unlockedMaps=" + joined;
        try {
            Files.write(
                    Paths.get(filePath),
                    Collections.singletonList(line),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to save progress to: " + filePath, e);
        }
    }

    public static boolean isMapUnlocked(int id) {
        return unlockedMaps.contains(id);
    }

    public static void unlockMap(int id) {
        if (unlockedMaps.add(id)) {
            save();
        }
    }

    public static Set<Integer> getUnlockedMaps() {
        return new HashSet<>(unlockedMaps);
    }

    public static boolean hasSavedGame() { return GameSaveManager.hasSave(); }
    public static void saveGame(GameSnapshot snapshot) { GameSaveManager.save(snapshot); }
    public static Optional<GameSnapshot> loadGame() { return GameSaveManager.load(); }
    public static void clearSavedGame() { GameSaveManager.clearSave(); }
    public static OptionalInt getSavedGameScore() { return GameSaveManager.getSavedScore(); }

    public static boolean isSavedGameDeveloperOnly() {
        Optional<GameSnapshot> opt = loadGame();
        if (opt.isEmpty()) return false;

        GameSnapshot s = opt.get();
        GameMode mode = (s.mode != null)
                ? s.mode
                : (s.settingsSnapshot != null ? s.settingsSnapshot.currentMode() : GameMode.STANDARD);
        int selectedMapId = (s.selectedMapId != 0)
                ? s.selectedMapId
                : (s.settingsSnapshot != null ? s.settingsSnapshot.selectedMapId() : 0);

        if (mode == GameMode.STANDARD) return false;
        return selectedMapId > 0 && !MapManager.isPackagedMapId(selectedMapId);
    }

    /**
     * Clears the saved game unless it is developer-only and the caller is not in developer mode.
     * Keeps developer-only saves hidden/preserved for normal users.
     */
    public static void clearSavedGameIfAllowed(boolean developerModeEnabled) {
        if (!developerModeEnabled && isSavedGameDeveloperOnly()) return;
        clearSavedGame();
    }

}
