package com.snakegame.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persists and provides the set of unlocked maps for MAP_SELECT and RACE modes.
 */
public class ProgressManager {
    private static final String FILE_PATH = "data/progress.txt";
    private static final Set<Integer> unlockedMaps = new HashSet<>();

    static {
        load();
    }

    public static void load() {
        Path path = Paths.get(FILE_PATH);
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
            e.printStackTrace();
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
                    Paths.get(FILE_PATH),
                    Collections.singletonList(line),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
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
}