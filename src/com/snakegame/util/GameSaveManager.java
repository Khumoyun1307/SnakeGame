package com.snakegame.util;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.model.*;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSaveManager {
    private static final String FILE_PATH = "data/savegame.txt";
    private static final Logger log = Logger.getLogger(GameSaveManager.class.getName());

    public static boolean hasSave() {
        return new File(FILE_PATH).exists();
    }

    public static void clearSave() {
        File f = new File(FILE_PATH);
        if (f.exists() && !f.delete()) {
            log.warning("Failed to delete save file: " + FILE_PATH);
        }
    }

    public static void save(GameSnapshot s) {
        if (s == null) return;

        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.severe("Failed to create save directory: " + parent.getPath());
            return;
        }

        Properties p = new Properties();

        // meta
        p.setProperty("savedAtMillis", String.valueOf(System.currentTimeMillis()));

        // settings snapshot (flattened)
        SettingsSnapshot ss = s.settingsSnapshot;
        if (ss != null) {
            p.setProperty("difficultyLevel", String.valueOf(ss.difficultyLevel()));
            p.setProperty("obstaclesEnabled", String.valueOf(ss.obstaclesEnabled()));
            p.setProperty("currentMode", ss.currentMode().name());
            p.setProperty("selectedMapId", String.valueOf(ss.selectedMapId()));
            p.setProperty("raceThreshold", String.valueOf(ss.raceThreshold()));
            p.setProperty("soundEnabled", String.valueOf(ss.soundEnabled()));
            p.setProperty("musicEnabled", String.valueOf(ss.musicEnabled()));
            p.setProperty("showGrid", String.valueOf(ss.showGrid()));
            p.setProperty("playerName", ss.playerName() == null ? "Player" : ss.playerName());
            p.setProperty("theme", ss.selectedTheme().name());
            p.setProperty("movingObstaclesEnabled", String.valueOf(ss.movingObstaclesEnabled()));
            p.setProperty("movingObstacleCount", String.valueOf(ss.movingObstacleCount()));
            p.setProperty("movingObstaclesAutoIncrement", String.valueOf(ss.movingObstaclesAutoIncrement()));
            p.setProperty("developerModeEnabled", String.valueOf(ss.developerModeEnabled()));
        }

        // gameplay
        p.setProperty("score", String.valueOf(s.score));
        p.setProperty("applesEaten", String.valueOf(s.applesEaten));
        p.setProperty("direction", (s.direction == null ? "RIGHT" : s.direction.name()));
        p.setProperty("snakeBody", encodePoints(s.snakeBody == null ? List.of() : s.snakeBody));

        // apple
        p.setProperty("applePos", encodePoint(s.applePos == null ? new Point(0, 0) : s.applePos));
        p.setProperty("appleType", (s.appleType == null ? "NORMAL" : s.appleType.name()));
        p.setProperty("appleSpawnTime", String.valueOf(s.appleSpawnTime));
        p.setProperty("appleVisibleDurationMs", String.valueOf(s.appleVisibleDurationMs));

        // effects
        p.setProperty("doubleScoreActive", String.valueOf(s.doubleScoreActive));
        p.setProperty("doubleScoreEndTime", String.valueOf(s.doubleScoreEndTime));
        p.setProperty("slowed", String.valueOf(s.slowed));
        p.setProperty("slowEndTime", String.valueOf(s.slowEndTime));
        p.setProperty("reversedControls", String.valueOf(s.reversedControls));
        p.setProperty("reverseEndTime", String.valueOf(s.reverseEndTime));

        // obstacles
        p.setProperty("obstacles", encodePoints(s.obstacles == null ? List.of() : s.obstacles));

        // moving obstacles
        List<MovingObstacleSnapshot> mosList = (s.movingObstacles == null) ? List.of() : s.movingObstacles;
        p.setProperty("mo.count", String.valueOf(mosList.size()));
        for (int i = 0; i < mosList.size(); i++) {
            MovingObstacleSnapshot mos = mosList.get(i);
            p.setProperty("mo." + i + ".segments", encodePoints(mos.segments == null ? List.of() : mos.segments));
            p.setProperty("mo." + i + ".dx", String.valueOf(mos.dx));
            p.setProperty("mo." + i + ".dy", String.valueOf(mos.dy));
        }

        try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            p.store(w, "Snake Game Save");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to save game", e);
        }
    }

    public static Optional<GameSnapshot> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return Optional.empty();

        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to load save", e);
            return Optional.empty();
        }

        try {
            GameSnapshot s = new GameSnapshot();

            s.savedAtMillis = Long.parseLong(p.getProperty("savedAtMillis", "0"));

            // restore SettingsSnapshot
            SettingsSnapshot ss = new SettingsSnapshot(
                    Integer.parseInt(p.getProperty("difficultyLevel", "20")),
                    Boolean.parseBoolean(p.getProperty("obstaclesEnabled", "false")),
                    com.snakegame.mode.GameMode.valueOf(p.getProperty("currentMode", "STANDARD")),
                    Integer.parseInt(p.getProperty("selectedMapId", "1")),
                    Integer.parseInt(p.getProperty("raceThreshold", "20")),
                    Boolean.parseBoolean(p.getProperty("soundEnabled", "true")),
                    Boolean.parseBoolean(p.getProperty("musicEnabled", "true")),
                    Boolean.parseBoolean(p.getProperty("showGrid", "true")),
                    p.getProperty("playerName", "Player"),
                    GameSettings.Theme.valueOf(p.getProperty("theme", "RETRO")),
                    Boolean.parseBoolean(p.getProperty("movingObstaclesEnabled", "false")),
                    Integer.parseInt(p.getProperty("movingObstacleCount", "0")),
                    Boolean.parseBoolean(p.getProperty("movingObstaclesAutoIncrement", "false")),
                    Boolean.parseBoolean(p.getProperty("developerModeEnabled", "false"))
            );
            s.settingsSnapshot = ss;

            // gameplay
            s.score = Integer.parseInt(p.getProperty("score", "0"));
            s.applesEaten = Integer.parseInt(p.getProperty("applesEaten", "0"));
            s.direction = Direction.valueOf(p.getProperty("direction", "RIGHT"));
            s.snakeBody = decodePoints(p.getProperty("snakeBody", ""));

            // apple
            s.applePos = decodePoint(p.getProperty("applePos", "0,0"));
            s.appleType = AppleType.valueOf(p.getProperty("appleType", "NORMAL"));
            s.appleSpawnTime = Long.parseLong(p.getProperty("appleSpawnTime", "0"));
            s.appleVisibleDurationMs = Long.parseLong(p.getProperty("appleVisibleDurationMs", "0"));

            // effects
            s.doubleScoreActive = Boolean.parseBoolean(p.getProperty("doubleScoreActive", "false"));
            s.doubleScoreEndTime = Long.parseLong(p.getProperty("doubleScoreEndTime", "0"));
            s.slowed = Boolean.parseBoolean(p.getProperty("slowed", "false"));
            s.slowEndTime = Long.parseLong(p.getProperty("slowEndTime", "0"));
            s.reversedControls = Boolean.parseBoolean(p.getProperty("reversedControls", "false"));
            s.reverseEndTime = Long.parseLong(p.getProperty("reverseEndTime", "0"));

            // obstacles
            s.obstacles = decodePoints(p.getProperty("obstacles", ""));

            // moving obstacles
            int moCount = Integer.parseInt(p.getProperty("mo.count", "0"));
            s.movingObstacles = new ArrayList<>();
            for (int i = 0; i < moCount; i++) {
                List<Point> segs = decodePoints(p.getProperty("mo." + i + ".segments", ""));
                int dx = Integer.parseInt(p.getProperty("mo." + i + ".dx", "0"));
                int dy = Integer.parseInt(p.getProperty("mo." + i + ".dy", "0"));
                s.movingObstacles.add(new MovingObstacleSnapshot(segs, dx, dy));
            }

            return Optional.of(s);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Save file corrupted, clearing: " + FILE_PATH, ex);
            clearSave();
            return Optional.empty();
        }
    }

    public static OptionalInt getSavedScore() {
        Optional<GameSnapshot> s = load();
        return s.isPresent() ? OptionalInt.of(s.get().score) : OptionalInt.empty();
    }

    // ---- encoding helpers ----

    private static String encodePoint(Point p) {
        return p.x + "," + p.y;
    }

    private static String encodePoints(List<Point> pts) {
        if (pts == null || pts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Point p : pts) {
            if (sb.length() > 0) sb.append(";");
            sb.append(p.x).append(",").append(p.y);
        }
        return sb.toString();
    }

    private static Point decodePoint(String s) {
        String[] parts = s.split(",");
        return new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }

    private static List<Point> decodePoints(String s) {
        List<Point> out = new ArrayList<>();
        if (s == null || s.isBlank()) return out;
        String[] pairs = s.split(";");
        for (String pair : pairs) {
            if (pair.isBlank()) continue;
            out.add(decodePoint(pair));
        }
        return out;
    }
}
